package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.core.profile.ProfileAfData;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppsFlyerLenderPayloadResolverTest {
    @Test
    void fillsBlankFieldsFromInstallCallback() {
        AppsFlyerCallbackRepository callbackRepository = mock(AppsFlyerCallbackRepository.class);
        AppsFlyerCallbackRepository.AppsFlyerCallbackData callback =
                mock(AppsFlyerCallbackRepository.AppsFlyerCallbackData.class);
        when(callback.appsflyerId()).thenReturn("af-1");
        when(callback.advertisingId()).thenReturn("gaid-from-cb");
        when(callback.installTime()).thenReturn("2026-07-01 00:00:00.000");
        when(callback.mediaSource()).thenReturn("facebook");
        when(callback.afPrt()).thenReturn("prt-cb");
        when(callback.afChannel()).thenReturn("channel-cb");
        when(callbackRepository.findLatestByAppsflyerIdAndEventName("af-1", "install"))
                .thenReturn(Optional.of(callback));

        AppsFlyerLenderPayloadResolver resolver = new AppsFlyerLenderPayloadResolver(callbackRepository);
        var payload = resolver.resolve(minimalAf("af-1", null, null, null));

        assertThat(payload.appsflyerId()).isEqualTo("af-1");
        assertThat(payload.mediaSource()).isEqualTo("facebook");
        assertThat(payload.afPrt()).isEqualTo("prt-cb");
        assertThat(payload.afChannel()).isEqualTo("channel-cb");
        assertThat(payload.advertisingId()).isEqualTo("gaid-from-cb");
        assertThat(payload.installTime()).isEqualTo("2026-07-01 00:00:00.000");
    }

    @Test
    void keepsProfileAfValueWhenAlreadyPresent() {
        AppsFlyerCallbackRepository callbackRepository = mock(AppsFlyerCallbackRepository.class);
        AppsFlyerCallbackRepository.AppsFlyerCallbackData callback =
                mock(AppsFlyerCallbackRepository.AppsFlyerCallbackData.class);
        when(callback.mediaSource()).thenReturn("from-callback");
        when(callbackRepository.findLatestByAppsflyerIdAndEventName("af-1", "install"))
                .thenReturn(Optional.of(callback));

        AppsFlyerLenderPayloadResolver resolver = new AppsFlyerLenderPayloadResolver(callbackRepository);
        assertThat(resolver.resolve(minimalAf("af-1", null, null, "from-client")).mediaSource())
                .isEqualTo("from-client");
    }

    @Test
    void fallsBackToAdvertisingIdWhenAppsflyerIdMisses() {
        AppsFlyerCallbackRepository callbackRepository = mock(AppsFlyerCallbackRepository.class);
        AppsFlyerCallbackRepository.AppsFlyerCallbackData callback =
                mock(AppsFlyerCallbackRepository.AppsFlyerCallbackData.class);
        when(callback.mediaSource()).thenReturn("organic");
        when(callback.installTime()).thenReturn("2026-07-17 00:00:00.000");
        when(callbackRepository.findLatestByAppsflyerIdAndEventName("af-client", "install"))
                .thenReturn(Optional.empty());
        when(callbackRepository.findLatestByAppsflyerId("af-client")).thenReturn(Optional.empty());
        when(callbackRepository.findLatestByAdvertisingIdAndEventName("gaid-1", "install"))
                .thenReturn(Optional.of(callback));

        AppsFlyerLenderPayloadResolver resolver = new AppsFlyerLenderPayloadResolver(callbackRepository);
        var payload = resolver.resolve(minimalAf("af-client", "gaid-1", "android-1", null));

        assertThat(payload.mediaSource()).isEqualTo("organic");
        assertThat(payload.installTime()).isEqualTo("2026-07-17 00:00:00.000");
    }

    private static ProfileAfData minimalAf(
            String appsflyerId, String advertisingId, String androidId, String mediaSource) {
        return new ProfileAfData(
                1L,
                2L,
                "dev-1",
                appsflyerId,
                advertisingId,
                androidId,
                null,
                null,
                null,
                mediaSource,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "COMPLETED",
                "req-1",
                null
        );
    }

    @Test
    void resolveInstallByDeviceNoUsesConversionTypeInstall() {
        AppsFlyerCallbackRepository callbackRepository = mock(AppsFlyerCallbackRepository.class);
        AppsFlyerCallbackRepository.AppsFlyerCallbackData callback =
                mock(AppsFlyerCallbackRepository.AppsFlyerCallbackData.class);
        when(callback.appsflyerId()).thenReturn("af-cb");
        when(callback.mediaSource()).thenReturn("facebook");
        when(callbackRepository.findLatestByDeviceNoAndConversionType("dev-1", "install"))
                .thenReturn(Optional.of(callback));

        AppsFlyerLenderPayloadResolver resolver = new AppsFlyerLenderPayloadResolver(callbackRepository);
        var payload = resolver.resolveInstallByDeviceNo("dev-1");

        assertThat(payload).isPresent();
        assertThat(payload.get().appsflyerId()).isEqualTo("af-cb");
        assertThat(payload.get().mediaSource()).isEqualTo("facebook");
    }
}
