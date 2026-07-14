package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthOtpConfigLoaderTest {
    @Mock
    private AppConfigRepository appConfigRepository;

    @Test
    void loadsOtpLimitsFromAppConfig() {
        when(appConfigRepository.findByKey(AuthOtpConfigLoader.CONFIG_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        AuthOtpConfigLoader.CONFIG_KEY,
                        """
                        {"otpDailyLimit":20,"otpResendIntervalSeconds":30,"otpDailyLimitZone":"Asia/Jakarta"}
                        """
                )
        ));

        AuthOtpConfigLoader.AuthOtpConfig config =
                new AuthOtpConfigLoader(appConfigRepository, new ObjectMapper()).load();

        assertThat(config.otpDailyLimit()).isEqualTo(20);
        assertThat(config.otpResendInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.otpDailyLimitZone()).isEqualTo(ZoneId.of("Asia/Jakarta"));
    }

    @Test
    void failsWhenConfigMissing() {
        when(appConfigRepository.findByKey(AuthOtpConfigLoader.CONFIG_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new AuthOtpConfigLoader(appConfigRepository, new ObjectMapper()).load())
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }
}
