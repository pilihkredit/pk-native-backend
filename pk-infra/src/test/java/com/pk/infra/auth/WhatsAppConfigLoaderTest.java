package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WhatsAppConfigLoaderTest {
    @Test
    void loadsWhatsAppConfFromAppConfig() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(WhatsAppConfigLoader.CONF_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        WhatsAppConfigLoader.CONF_KEY,
                        """
                        {
                          "enableWhatsApp": true,
                          "url": "https://example.com/v3",
                          "appKey": "k1",
                          "authorization": "a1",
                          "wabaId": "w1",
                          "sendNumber": "86123",
                          "templateGroupId": "group_otp_x",
                          "language": "en",
                          "countryDialCode": "62",
                          "timeout": 8000,
                          "minInterval": 90,
                          "expireTime": 180,
                          "defaultCode": "2460",
                          "userList": ["8123456789"]
                        }
                        """
                )
        ));

        WhatsAppConfigLoader.WhatsAppConf conf =
                new WhatsAppConfigLoader(repository, new ObjectMapper()).loadConf();

        assertThat(conf.enableWhatsApp()).isTrue();
        assertThat(conf.url()).isEqualTo("https://example.com/v3");
        assertThat(conf.appKey()).isEqualTo("k1");
        assertThat(conf.templateGroupId()).isEqualTo("group_otp_x");
        assertThat(conf.minInterval()).isEqualTo(Duration.ofSeconds(90));
        assertThat(conf.expireTime()).isEqualTo(Duration.ofSeconds(180));
        assertThat(conf.timeout()).isEqualTo(Duration.ofMillis(8000));
        assertThat(conf.defaultCode()).isEqualTo("2460");
        assertThat(conf.userList()).containsExactly("8123456789");
        assertThat(WhatsAppConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "2460")).isTrue();
        assertThat(WhatsAppConfigLoader.acceptsConfiguredDefaultCode(conf, "8999999999", "2460")).isFalse();
    }

    @Test
    void throwsWhenWhatsAppConfMissing() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(WhatsAppConfigLoader.CONF_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new WhatsAppConfigLoader(repository, new ObjectMapper()).loadConf())
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }

    @Test
    void loadsDailyLimitAsJsonNumber() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(WhatsAppConfigLoader.DAILY_LIMIT_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(2L, WhatsAppConfigLoader.DAILY_LIMIT_KEY, "8")
        ));

        assertThat(new WhatsAppConfigLoader(repository, new ObjectMapper()).loadDailyLimit()).isEqualTo(8);
    }

    @Test
    void defaultsDailyLimitWhenMissing() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(WhatsAppConfigLoader.DAILY_LIMIT_KEY)).thenReturn(Optional.empty());

        assertThat(new WhatsAppConfigLoader(repository, new ObjectMapper()).loadDailyLimit()).isEqualTo(5);
    }
}
