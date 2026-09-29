package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FaceVerifyTicketTtlConfigLoaderTest {

    @Test
    void usesDefaultsWhenConfigMissing() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(FaceVerifyTicketTtlConfigLoader.CONFIG_KEY)).thenReturn(Optional.empty());

        FaceVerifyTicketTtlConfigLoader loader =
                new FaceVerifyTicketTtlConfigLoader(repository, new ObjectMapper());

        assertThat(loader.bankCardAddTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(loader.deviceSwitchLoginTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(loader.mobileChangeTtl()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void appliesPerSceneOverridesFromJson() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(FaceVerifyTicketTtlConfigLoader.CONFIG_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        FaceVerifyTicketTtlConfigLoader.CONFIG_KEY,
                        """
                        {"ttlMinutes":5,"bankCardAddMinutes":15,"mobileChangeMinutes":10}
                        """
                )
        ));

        FaceVerifyTicketTtlConfigLoader loader =
                new FaceVerifyTicketTtlConfigLoader(repository, new ObjectMapper());

        assertThat(loader.deviceSwitchLoginTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(loader.bankCardAddTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(loader.mobileChangeTtl()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void acceptsPlainNumberAsUniformTtlMinutes() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(FaceVerifyTicketTtlConfigLoader.CONFIG_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, FaceVerifyTicketTtlConfigLoader.CONFIG_KEY, "7")
        ));

        FaceVerifyTicketTtlConfigLoader loader =
                new FaceVerifyTicketTtlConfigLoader(repository, new ObjectMapper());

        assertThat(loader.bankCardAddTtl()).isEqualTo(Duration.ofMinutes(7));
    }
}
