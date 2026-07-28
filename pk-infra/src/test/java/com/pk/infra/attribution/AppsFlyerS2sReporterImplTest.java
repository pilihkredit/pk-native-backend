package com.pk.infra.attribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.attribution.port.AdjustConfigRepository;
import com.pk.core.attribution.port.AdjustEventConfigRepository;
import com.pk.core.attribution.port.AdjustEventRecordRepository;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AppsFlyerS2sReporterImplTest {
    private AdjustConfigRepository adjustConfigRepository;
    private AdjustEventConfigRepository adjustEventConfigRepository;
    private AdjustEventRecordRepository adjustEventRecordRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private ProfileAfRepository profileAfRepository;
    private AppsFlyerS2sReporterImpl reporter;

    @BeforeEach
    void setUp() {
        adjustConfigRepository = mock(AdjustConfigRepository.class);
        adjustEventConfigRepository = mock(AdjustEventConfigRepository.class);
        adjustEventRecordRepository = mock(AdjustEventRecordRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        profileAfRepository = mock(ProfileAfRepository.class);
        reporter = new AppsFlyerS2sReporterImpl(
                adjustConfigRepository,
                adjustEventConfigRepository,
                adjustEventRecordRepository,
                profileDeviceRepository,
                profileAfRepository,
                new ObjectMapper()
        );
    }

    @Test
    void skipsWhenEventNotEnabled() {
        when(adjustConfigRepository.findActiveByOsName("Android"))
                .thenReturn(Optional.of(new AdjustConfigRepository.AdjustConfigData(
                        1L, "app-id", "dev-key", "https://api2.appsflyer.com/inappevent/app-id", 30000, "Android"
                )));
        when(adjustEventConfigRepository.findEnabledByEventNameAndAppToken("BASIC_AUTH_FINISH", "app-id"))
                .thenReturn(Optional.empty());

        AppsFlyerS2sReporter.ReportResult result = reporter.report(10L, parsedEvent());

        assertThat(result.reported()).isFalse();
        assertThat(result.message()).contains("event disabled");
        verify(adjustEventRecordRepository, never()).insert(any());
    }

    @Test
    void insertsPendingRecordUsingDeviceAppsflyerId() {
        when(adjustConfigRepository.findActiveByOsName("Android"))
                .thenReturn(Optional.of(new AdjustConfigRepository.AdjustConfigData(
                        1L, "app-id", "dev-key", "https://example.invalid/inappevent/app-id", 1000, "Android"
                )));
        when(adjustEventConfigRepository.findEnabledByEventNameAndAppToken("BASIC_AUTH_FINISH", "app-id"))
                .thenReturn(Optional.of(new AdjustEventConfigRepository.AdjustEventConfigData(
                        2L, "BASIC_AUTH_FINISH", "app-id", true
                )));
        when(profileDeviceRepository.findByDeviceNo("DEVICE202506020001"))
                .thenReturn(Optional.of(new ProfileDeviceData(
                        22L,
                        "OPEN_USER_10001",
                        "DEVICE202506020001",
                        "android",
                        "cashloan",
                        "4.8.0",
                        "com.pk.app",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "gaid-demo-value",
                        "idfv-1",
                        "idfa-1",
                        null,
                        null,
                        "{\"appsflyerId\":\"af-device-1\",\"idfv\":\"idfv-1\",\"idfa\":\"idfa-1\"}",
                        "req-1"
                )));
        when(adjustEventRecordRepository.insert(any())).thenReturn(99L);

        AppsFlyerS2sReporter.ReportResult result = reporter.report(10L, parsedEvent());

        assertThat(result.reported()).isTrue();
        assertThat(result.recordId()).isEqualTo(99L);
        ArgumentCaptor<AdjustEventRecordRepository.AdjustEventRecordInsert> captor =
                ArgumentCaptor.forClass(AdjustEventRecordRepository.AdjustEventRecordInsert.class);
        verify(adjustEventRecordRepository).insert(captor.capture());
        assertThat(captor.getValue().serverEventCallbackId()).isEqualTo(10L);
        assertThat(captor.getValue().eventName()).isEqualTo("BASIC_AUTH_FINISH");
        assertThat(captor.getValue().adid()).isEqualTo("af-device-1");
        assertThat(captor.getValue().idfa()).isEqualTo("idfa-1");
        assertThat(captor.getValue().idfv()).isEqualTo("idfv-1");
        verify(adjustEventRecordRepository).updateStatus(eq(99L), anyInt(), any(), any(), eq(0));
    }

    @Test
    void insertsPendingRecordUsingUserProfileAfAppsflyerId() {
        when(adjustConfigRepository.findActiveByOsName("Android"))
                .thenReturn(Optional.of(new AdjustConfigRepository.AdjustConfigData(
                        1L, "app-id", "dev-key", "https://example.invalid/inappevent/app-id", 1000, "Android"
                )));
        when(adjustEventConfigRepository.findEnabledByEventNameAndAppToken("BASIC_AUTH_FINISH", "app-id"))
                .thenReturn(Optional.of(new AdjustEventConfigRepository.AdjustEventConfigData(
                        2L, "BASIC_AUTH_FINISH", "app-id", true
                )));
        when(profileDeviceRepository.findByDeviceNo("DEVICE202506020001"))
                .thenReturn(Optional.empty());
        ProfileAfData afData = mock(ProfileAfData.class);
        when(afData.appsflyerId()).thenReturn("af-from-install");
        when(afData.advertisingId()).thenReturn("gaid-1");
        when(profileAfRepository.findLatestByDeviceNo("DEVICE202506020001"))
                .thenReturn(Optional.of(afData));
        when(adjustEventRecordRepository.insert(any())).thenReturn(88L);

        AppsFlyerS2sReporter.ReportResult result = reporter.report(10L, parsedEventWithoutAdId());

        assertThat(result.reported()).isTrue();
        ArgumentCaptor<AdjustEventRecordRepository.AdjustEventRecordInsert> captor =
                ArgumentCaptor.forClass(AdjustEventRecordRepository.AdjustEventRecordInsert.class);
        verify(adjustEventRecordRepository).insert(captor.capture());
        assertThat(captor.getValue().adid()).isEqualTo("af-from-install");
        assertThat(captor.getValue().gpsAdid()).isEqualTo("gaid-1");
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

    private static ServerEventCallbackParser.ParsedServerEventCallback parsedEventWithoutAdId() {
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
                null
        );
    }
}
