package com.pk.infra.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.push.PushDeviceTarget;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.FcmPushPort;
import com.pk.core.push.port.PushAudienceRepository;
import com.pk.core.push.port.PushDeviceRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PushDeliveryFacadeTest {
    private FcmPushPort fcmPushPort;
    private PushDeviceRepository pushDeviceRepository;
    private PushAudienceRepository pushAudienceRepository;
    private PushNotificationTaskRepository taskRepository;
    private PushDeliveryFacade facade;

    @BeforeEach
    void setUp() {
        fcmPushPort = mock(FcmPushPort.class);
        pushDeviceRepository = mock(PushDeviceRepository.class);
        pushAudienceRepository = mock(PushAudienceRepository.class);
        taskRepository = mock(PushNotificationTaskRepository.class);
        facade = new PushDeliveryFacade(
                fcmPushPort,
                pushDeviceRepository,
                pushAudienceRepository,
                taskRepository,
                new PushPayloadAssembler(),
                2
        );
        when(fcmPushPort.send(any())).thenReturn(new FcmPushPort.FcmSendResult(true, "msg", "ok"));
    }

    @Test
    void deliversToAllDevicesWithKeysetPaginationAndDedupTokens() {
        when(pushDeviceRepository.findTargetsAfterId(0L, 500)).thenReturn(List.of(
                new PushDeviceTarget(1L, 10L, "token-a"),
                new PushDeviceTarget(2L, 11L, "token-b")
        ));
        when(pushDeviceRepository.findTargetsAfterId(2L, 500)).thenReturn(List.of(
                new PushDeviceTarget(3L, 12L, "token-a"),
                new PushDeviceTarget(4L, null, "token-c")
        ));
        when(pushDeviceRepository.findTargetsAfterId(4L, 500)).thenReturn(List.of());

        var result = facade.deliver(task(PushNotificationTask.TYPE_ALL, "all"));

        assertThat(result.totalCount()).isEqualTo(3);
        assertThat(result.successCount()).isEqualTo(3);
        assertThat(result.failureCount()).isZero();
        verify(fcmPushPort, times(3)).send(any());
    }

    @Test
    void deliversUploadAudienceThroughUserIds() {
        when(pushAudienceRepository.findUserIdsByTaskId(1001L)).thenReturn(List.of(10L, 11L));
        when(pushDeviceRepository.findTargetsByUserId(10L))
                .thenReturn(List.of(new PushDeviceTarget(1L, 10L, "token-a")));
        when(pushDeviceRepository.findTargetsByUserId(11L))
                .thenReturn(List.of(new PushDeviceTarget(2L, 11L, "token-b")));

        var result = facade.deliver(task(PushNotificationTask.TYPE_INTERNAL, "upload"));

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(2);
        verify(pushDeviceRepository, never()).findTargetsAfterId(anyLong(), anyInt());
    }

    @Test
    void clearRequiredMarksTargetsClearedBeforeSendingCancel() {
        var clearTask = new PushNotificationTask(
                1004L, "cancel", "", "", "", PushNotificationTask.TYPE_CLEAR_REQUIRED, false,
                List.of(), null, null, null, null, "all", null, List.of(1001L, 1002L), null,
                PushNotificationTask.STATUS_SENDING, null, 0, 0, 0, null, Instant.now(), null
        );
        when(taskRepository.markCleared(any(), any())).thenReturn(2);
        when(pushDeviceRepository.findTargetsAfterId(0L, 500))
                .thenReturn(List.of(new PushDeviceTarget(1L, 10L, "token-a")));

        var result = facade.deliver(clearTask);

        verify(taskRepository).markCleared(eq(List.of(1001L, 1002L)), any());
        ArgumentCaptor<FcmPushPort.FcmSendCommand> captor =
                ArgumentCaptor.forClass(FcmPushPort.FcmSendCommand.class);
        verify(fcmPushPort).send(captor.capture());
        assertThat(captor.getValue().data().get("type")).isEqualTo("IN_APP_POPUP_CANCEL");
        assertThat(captor.getValue().data().get("popupIds")).isEqualTo("1001,1002");
        assertThat(result.successCount()).isEqualTo(1);
    }

    @Test
    void countsFailuresWhenFcmRejectsOrThrows() {
        AtomicInteger calls = new AtomicInteger();
        when(fcmPushPort.send(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                throw new IllegalStateException("boom");
            }
            return new FcmPushPort.FcmSendResult(false, null, "unregistered");
        });
        when(pushDeviceRepository.findTargetsAfterId(0L, 500)).thenReturn(List.of(
                new PushDeviceTarget(1L, 10L, "token-a"),
                new PushDeviceTarget(2L, 10L, "token-b")
        ));
        when(pushDeviceRepository.findTargetsAfterId(2L, 500)).thenReturn(List.of());

        var result = facade.deliver(task(PushNotificationTask.TYPE_EXTERNAL, "all"));

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.successCount()).isZero();
        assertThat(result.failureCount()).isEqualTo(2);
    }

    @Test
    void noRecipientsShortCircuits() {
        when(pushDeviceRepository.findTargetsAfterId(0L, 500)).thenReturn(List.of());

        var result = facade.deliver(task(PushNotificationTask.TYPE_INTERNAL, "all"));

        assertThat(result.totalCount()).isZero();
        verify(fcmPushPort, never()).send(any());
    }

    private static PushNotificationTask task(String type, String audienceType) {
        return new PushNotificationTask(
                1001L,
                "task-name",
                "Loan approved",
                "Your loan has been approved",
                "Lihat",
                type,
                true,
                List.of("/profile"),
                "/h5/popup",
                "/profile/loan-history/123",
                "/repay?from=notify",
                null,
                audienceType,
                null,
                List.of(),
                null,
                PushNotificationTask.STATUS_SENDING,
                null,
                0,
                0,
                0,
                null,
                Instant.now(),
                null
        );
    }
}
