package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SmsConfigLoaderTest {
    @Test
    void loadsUserListAndDefaultCode() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("smsConf")).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        "smsConf",
                        """
                        {"enableSms":true,"url":"https://example.com","spid":"s","pwd":"p",
                         "commercialCode":"0062","expireTime":300,"timeout":10000,
                         "defaultCode":"9999","userList":["8123456789","8999999999"]}
                        """
                )
        ));

        SmsConfigLoader.SmsConf conf = new SmsConfigLoader(repository, new ObjectMapper()).loadConf();

        assertThat(conf.defaultCode()).isEqualTo("9999");
        assertThat(conf.userList()).containsExactly("8123456789", "8999999999");
        assertThat(conf.codeLength()).isEqualTo(6);
    }

    @Test
    void loadsCodeLengthFromConfig() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("smsConf")).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        "smsConf",
                        """
                        {"enableSms":true,"url":"https://example.com","spid":"s","pwd":"p",
                         "commercialCode":"0062","expireTime":300,"timeout":10000,
                         "defaultCode":"2460","codeLength":4,"userList":[]}
                        """
                )
        ));

        SmsConfigLoader.SmsConf conf = new SmsConfigLoader(repository, new ObjectMapper()).loadConf();
        assertThat(conf.codeLength()).isEqualTo(4);
        assertThat(conf.contentTemplate()).contains("{code}");
    }

    @Test
    void acceptsWhitelistDefaultCodeOnlyForListedMobile() {
        SmsConfigLoader.SmsConf conf = new SmsConfigLoader.SmsConf(
                true, "", "", "", "0062", "", 300, 10_000, "1234", 4, List.of("8123456789")
        );
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "1234")).isTrue();
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8999999999", "1234")).isFalse();
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "0000")).isFalse();
    }

    @Test
    void acceptsNonWhitelistDefaultCodeWhenSmsDisabled() {
        // enableSms=false → credit-core parity: anyone may use defaultCode
        SmsConfigLoader.SmsConf conf = new SmsConfigLoader.SmsConf(
                false, "", "", "", "0062", "", 300, 10_000, "1234", 4, List.of("8123456789")
        );
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "1234")).isTrue();
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8999999999", "1234")).isTrue();
    }

    @Test
    void acceptsDefaultCodeForAllWhenSmsDisabled() {
        SmsConfigLoader.SmsConf conf = new SmsConfigLoader.SmsConf(
                false, "", "", "", "0062", "", 300, 10_000, "1234", 6, List.of()
        );
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "1234")).isTrue();
        assertThat(SmsConfigLoader.acceptsConfiguredDefaultCode(conf, "8123456789", "0000")).isFalse();
    }
}
