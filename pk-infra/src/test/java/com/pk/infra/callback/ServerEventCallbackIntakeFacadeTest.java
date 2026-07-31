package com.pk.infra.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.port.LenderServerEventCallbackRepository;
import com.pk.core.callback.port.ServerEventCallbackParser;
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
    private LenderServerEventCallbackRepository lenderServerEventCallbackRepository;
    @Mock
    private ServerEventCallbackParser serverEventCallbackParser;
    @Mock
    private AppsFlyerS2sReporter appsFlyerS2sReporter;

    private ServerEventCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new ServerEventCallbackIntakeFacade(
                lenderServerEventCallbackRepository,
                serverEventCallbackParser,
                appsFlyerS2sReporter
        );
    }

    @Test
    void storesNewEventPushWithDocumentFields() {
        when(serverEventCallbackParser.parse("{}")).thenReturn(parsedEvent());
        when(lenderServerEventCallbackRepository.findByEventId("USR202506020001")).thenReturn(Optional.empty());
        when(lenderServerEventCallbackRepository.insert(any())).thenReturn(42L);
        when(appsFlyerS2sReporter.report(any(Long.class), any()))
                .thenReturn(AppsFlyerS2sReporter.ReportResult.recorded(9L, "OK"));

        ServerEventCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.serverEventCallbackId()).isEqualTo(42L);
        assertThat(result.duplicate()).isFalse();
        ArgumentCaptor<LenderServerEventCallbackRepository.LenderServerEventCallbackInsert> captor =
                ArgumentCaptor.forClass(LenderServerEventCallbackRepository.LenderServerEventCallbackInsert.class);
        verify(lenderServerEventCallbackRepository).insert(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo("USR202506020001");
        assertThat(captor.getValue().eventType()).isEqualTo("BASIC_AUTH_FINISH");
        assertThat(captor.getValue().eventTime()).isEqualTo(1749792000000L);
        assertThat(captor.getValue().value()).isEqualByComparingTo("10.5");
        assertThat(captor.getValue().partnerUserId()).isEqualTo("OPEN_USER_10001");
        assertThat(captor.getValue().deviceNo()).isEqualTo("DEVICE202506020001");
        assertThat(captor.getValue().systemPlatform()).isEqualTo("Android");
        assertThat(captor.getValue().adId()).isEqualTo("gaid-demo-value");
        verify(appsFlyerS2sReporter).report(eq(42L), any());
    }

    @Test
    void returnsExistingRecordForDuplicateEventPush() {
        when(serverEventCallbackParser.parse("{}")).thenReturn(parsedEvent());
        when(lenderServerEventCallbackRepository.findByEventId("USR202506020001"))
                .thenReturn(Optional.of(new LenderServerEventCallbackRepository.LenderServerEventCallbackRecord(
                        7L, "USR202506020001", "BASIC_AUTH_FINISH"
                )));

        ServerEventCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.serverEventCallbackId()).isEqualTo(7L);
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
}
