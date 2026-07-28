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
                  "media_source": "facebook",
                  "event_name": "install",
                  "customer_user_id": "U100",
                  "af_prt": "partner-a",
                  "af_adset_id": "adset-1",
                  "af_channel": "social",
                  "advertising_id": "gaid-1",
                  "install_time": "2026-07-28 10:00:00.000",
                  "contributor1_touch_type": "click"
                }
                """;

        AppsFlyerCallbackIntakeFacade.IntakeResult result = facade.intake(raw);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.status()).isEqualTo("processed");
        ArgumentCaptor<AppsFlyerCallbackRepository.AppsFlyerCallbackInsert> captor =
                ArgumentCaptor.forClass(AppsFlyerCallbackRepository.AppsFlyerCallbackInsert.class);
        verify(repository).insert(captor.capture());
        AppsFlyerCallbackRepository.AppsFlyerCallbackInsert insert = captor.getValue();
        assertThat(insert.userId()).isEqualTo(22L);
        assertThat(insert.deviceNo()).isEqualTo("DEVICE-9");
        assertThat(insert.appsflyerId()).isEqualTo("af-1");
        assertThat(insert.mediaSource()).isEqualTo("facebook");
        assertThat(insert.eventName()).isEqualTo("install");
        assertThat(insert.customerUserId()).isEqualTo("U100");
        assertThat(insert.afPrt()).isEqualTo("partner-a");
        assertThat(insert.afAdsetId()).isEqualTo("adset-1");
        assertThat(insert.afChannel()).isEqualTo("social");
        assertThat(insert.advertisingId()).isEqualTo("gaid-1");
        assertThat(insert.contributor1TouchType()).isEqualTo("click");
        assertThat(insert.rawData()).isEqualTo(raw);
        assertThat(insert.callbackStatus()).isEqualTo("processed");
    }
}
