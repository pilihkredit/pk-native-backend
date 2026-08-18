package com.pk.app.debug.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugPushSendRequest;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.port.FcmPushPort;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DebugPushSendApplicationServiceTest {
    private DebugUserProgressProperties properties;
    private FcmPushPort fcmPushPort;
    private DebugPushSendApplicationService service;

    @BeforeEach
    void setUp() {
        properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        fcmPushPort = mock(FcmPushPort.class);
        service = new DebugPushSendApplicationService(properties, fcmPushPort);
    }

    @Test
    void sendsPushWhenTokenValid() {
        when(fcmPushPort.send(any())).thenReturn(new FcmPushPort.FcmSendResult(true, "projects/x/messages/1", "{}"));

        var response = service.send("debug-token", new DebugPushSendRequest(
                "device-token",
                "Title",
                "Body",
                Map.of("type", "LOAN_STATUS", "messageId", "10025")
        ));

        assertThat(response.success()).isTrue();
        assertThat(response.messageName()).isEqualTo("projects/x/messages/1");
        ArgumentCaptor<FcmPushPort.FcmSendCommand> captor = ArgumentCaptor.forClass(FcmPushPort.FcmSendCommand.class);
        verify(fcmPushPort).send(captor.capture());
        assertThat(captor.getValue().fcmToken()).isEqualTo("device-token");
        assertThat(captor.getValue().data()).containsEntry("type", "LOAN_STATUS");
    }

    @Test
    void rejectsInvalidDebugToken() {
        assertThatThrownBy(() -> service.send("bad", new DebugPushSendRequest("t", "a", "b", Map.of())))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.UNAUTHORIZED_REQUEST);
    }
}
