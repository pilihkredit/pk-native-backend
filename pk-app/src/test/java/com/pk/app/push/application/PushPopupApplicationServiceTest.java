package com.pk.app.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.PushDisplayLogRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PushPopupApplicationServiceTest {
    private PushNotificationTaskRepository taskRepository;
    private PushDisplayLogRepository displayLogRepository;
    private PushPopupApplicationService service;

    @BeforeEach
    void setUp() {
        taskRepository = mock(PushNotificationTaskRepository.class);
        displayLogRepository = mock(PushDisplayLogRepository.class);
        service = new PushPopupApplicationService(taskRepository, displayLogRepository);
    }

    @Test
    void displayedIsIdempotent() {
        when(taskRepository.findById(1001L)).thenReturn(Optional.of(mandatoryTask()));
        when(displayLogRepository.insertDisplayedIfAbsent(eq(10L), eq(1001L), any()))
                .thenReturn(true)
                .thenReturn(false);

        assertThat(service.markDisplayed(principal(), 1001L).recorded()).isTrue();
        assertThat(service.markDisplayed(principal(), 1001L).recorded()).isFalse();
    }

    @Test
    void rejectsDisplayedForNonMandatoryTask() {
        when(taskRepository.findById(1002L))
                .thenReturn(Optional.of(externalOnlyTask()));

        assertThatThrownBy(() -> service.markDisplayed(principal(), 1002L))
                .isInstanceOf(ApiException.class);
        verify(displayLogRepository, never()).insertDisplayedIfAbsent(anyLong(), anyLong(), any());
    }

    @Test
    void rejectsMissingPrincipal() {
        assertThatThrownBy(() -> service.markDisplayed(null, 1001L))
                .isInstanceOf(ApiException.class);
    }

    private static AuthenticatedPrincipal principal() {
        return new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L);
    }

    private static PushNotificationTask mandatoryTask() {
        return new PushNotificationTask(
                1001L, "t", "Loan approved", "Your loan has been approved", "Lihat",
                PushNotificationTask.TYPE_ALL, true, List.of("/profile"),
                null, "/profile/loan-history/123", "/repay?from=notify", null,
                "all", null, List.of(), null,
                PushNotificationTask.STATUS_COMPLETED, null, 1, 1, 0,
                null, Instant.now(), Instant.now()
        );
    }

    private static PushNotificationTask externalOnlyTask() {
        return new PushNotificationTask(
                1002L, "t", "title", "body", "", PushNotificationTask.TYPE_EXTERNAL, false,
                List.of(), null, null, "/repay", null, "all", null, List.of(), null,
                PushNotificationTask.STATUS_COMPLETED, null, 1, 1, 0,
                null, Instant.now(), Instant.now()
        );
    }
}
