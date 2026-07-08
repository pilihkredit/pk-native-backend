package com.pk.infra.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerEventCallbackIntakeFacadeTest {
    @Mock
    private CallbackEventRepository callbackEventRepository;
    @Mock
    private ServerEventCallbackParser serverEventCallbackParser;
    @Mock
    private AppsFlyerS2sReporter appsFlyerS2sReporter;

    private ServerEventCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new ServerEventCallbackIntakeFacade(
                callbackEventRepository,
                serverEventCallbackParser,
                appsFlyerS2sReporter
        );
    }

    @Test
    void storesNewEventPushAsProcessedCallbackEvent() {
        when(serverEventCallbackParser.parse("{}")).thenReturn(parsedEvent());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(callbackEventRepository.insert(any())).thenReturn(42L);
        when(appsFlyerS2sReporter.report(any(Long.class), any()))
                .thenReturn(AppsFlyerS2sReporter.ReportResult.recorded(9L, "OK"));

        ServerEventCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(42L);
        assertThat(result.duplicate()).isFalse();
        ArgumentCaptor<CallbackEventRepository.CallbackEventInsert> captor =
                ArgumentCaptor.forClass(CallbackEventRepository.CallbackEventInsert.class);
        verify(callbackEventRepository).insert(captor.capture());
        assertThat(captor.getValue().providerCode()).isEqualTo(CreditProviderCode.PENDANAAN);
        assertThat(captor.getValue().callbackType()).isEqualTo(CallbackTypes.EVENT_PUSH);
        assertThat(captor.getValue().businessId()).isEqualTo("USR202506020001");
        assertThat(captor.getValue().idempotencyKey())
                .isEqualTo("pendanaan:EVENT_PUSH:USR202506020001");
        assertThat(captor.getValue().externalStatus()).isEqualTo("BASIC_AUTH_FINISH");
        assertThat(captor.getValue().payloadJson()).isEqualTo("{}");
        assertThat(captor.getValue().processStatus()).isEqualTo(CallbackProcessStatus.PROCESSED);
        verify(appsFlyerS2sReporter).report(eq(42L), any());
    }

    @Test
    void returnsExistingRecordForDuplicateEventPush() {
        when(serverEventCallbackParser.parse("{}")).thenReturn(parsedEvent());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existingRecord()));

        ServerEventCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(7L);
        assertThat(result.duplicate()).isTrue();
        verifyNoInteractions(appsFlyerS2sReporter);
    }

    private static ServerEventCallbackParser.ParsedServerEventCallback parsedEvent() {
        return new ServerEventCallbackParser.ParsedServerEventCallback(
                "USR202506020001",
                "BASIC_AUTH_FINISH",
                1749792000000L,
                null,
                new BigDecimal("10.5"),
                "open_client_demo",
                "USR202506020001",
                "OPEN_USER_10001",
                "cashloan",
                "ID",
                "4.8.0",
                "Indonesia",
                "DEVICE202506020001",
                "Android",
                "gaid-demo-value"
        );
    }

    private static CallbackEventRepository.CallbackEventRecord existingRecord() {
        return new CallbackEventRepository.CallbackEventRecord(
                7L,
                "CB-1",
                CreditProviderCode.PENDANAAN,
                CallbackTypes.EVENT_PUSH,
                "USR202506020001",
                "pendanaan:EVENT_PUSH:USR202506020001",
                "BASIC_AUTH_FINISH",
                "{}",
                CallbackProcessStatus.PROCESSED
        );
    }
}
