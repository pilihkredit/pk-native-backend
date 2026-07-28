package com.pk.infra.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.port.ProfileAfRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AppsFlyerCallbackIntakeFacadeTest {
    private AppsFlyerCallbackRepository repository;
    private ProfileAfRepository profileAfRepository;
    private AppsFlyerCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        repository = mock(AppsFlyerCallbackRepository.class);
        profileAfRepository = mock(ProfileAfRepository.class);
        facade = new AppsFlyerCallbackIntakeFacade(repository, profileAfRepository, new ObjectMapper());
    }

    @Test
    void parsesLenderRelevantFieldsAndPersists() {
        when(repository.insert(any())).thenReturn(42L);
        ProfileAfData localAf = mock(ProfileAfData.class);
        when(localAf.userId()).thenReturn(22L);
        when(localAf.deviceNo()).thenReturn("DEVICE-9");
        when(profileAfRepository.findLatestByAppsflyerId("af-1")).thenReturn(Optional.of(localAf));
        String raw = """
                {
                  "appsflyer_id": "af-1",
                  "media_source": "organic",
                  "event_name": "install",
                  "event_type": "organic-install",
                  "event_source": "SDK",
                  "event_time": "2026-07-28 12:41:41.200",
                  "app_name": "Pilih Kredit",
                  "campaign_type": "organic",
                  "conversion_type": "install",
                  "is_retargeting": false,
                  "wifi": false,
                  "region": "AS",
                  "state": "JK",
                  "dma": "360155",
                  "carrier": "3",
                  "language": "Indonesia",
                  "gp_referrer": "utm_source=google-play",
                  "sdk_version": "v6.17.3",
                  "api_version": "2.0",
                  "user_agent": "Dalvik/2.1.0",
                  "selected_timezone": "Asia/Shanghai",
                  "selected_currency": "USD",
                  "customer_user_id": "U100",
                  "advertising_id": "gaid-1",
                  "install_time": "2026-07-28 10:00:00.000"
                }
                """;

        AppsFlyerCallbackIntakeFacade.IntakeResult result = facade.intake(raw);

        assertThat(result.id()).isEqualTo(42L);
        ArgumentCaptor<AppsFlyerCallbackRepository.AppsFlyerCallbackInsert> captor =
                ArgumentCaptor.forClass(AppsFlyerCallbackRepository.AppsFlyerCallbackInsert.class);
        verify(repository).insert(captor.capture());
        AppsFlyerCallbackRepository.AppsFlyerCallbackInsert insert = captor.getValue();
        assertThat(insert.userId()).isEqualTo(22L);
        assertThat(insert.deviceNo()).isEqualTo("DEVICE-9");
        assertThat(insert.appsflyerId()).isEqualTo("af-1");
        assertThat(insert.mediaSource()).isEqualTo("organic");
        assertThat(insert.eventName()).isEqualTo("install");
        assertThat(insert.eventSource()).isEqualTo("SDK");
        assertThat(insert.eventTime()).isEqualTo("2026-07-28 12:41:41.200");
        assertThat(insert.appName()).isEqualTo("Pilih Kredit");
        assertThat(insert.campaignType()).isEqualTo("organic");
        assertThat(insert.conversionType()).isEqualTo("install");
        assertThat(insert.isRetargeting()).isFalse();
        assertThat(insert.wifi()).isFalse();
        assertThat(insert.region()).isEqualTo("AS");
        assertThat(insert.gpReferrer()).isEqualTo("utm_source=google-play");
        assertThat(insert.sdkVersion()).isEqualTo("v6.17.3");
        assertThat(insert.selectedCurrency()).isEqualTo("USD");
        assertThat(insert.rawData()).isEqualTo(raw);
    }
}
