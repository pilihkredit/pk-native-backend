package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.auth.SmsSendResult;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GatewaySmsSenderTest {
    @Test
    void buildsSameIndonesianTemplateAsCreditCore() {
        String content = GatewaySmsSender.buildSmsContent(
                SmsConfigLoader.DEFAULT_CONTENT_TEMPLATE,
                "123456",
                300
        );
        assertThat(content).isEqualTo(
                "[PilihKredit] Kode verifikasi Anda adalah 123456 valid selama 5 menit. "
                        + "JANGAN Bagikan kode ini kepada siapapun!"
        );
    }

    @Test
    void stringToHexMatchesCreditCoreUtf16CodeUnits() {
        assertThat(GatewaySmsSender.stringToHex("A")).isEqualTo("41");
        assertThat(GatewaySmsSender.stringToHex("ab")).isEqualTo("6162");
    }

    @Test
    void mocksWhenEnableSmsFalse() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("smsConf")).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        "smsConf",
                        """
                        {"enableSms":false,"url":"https://example.com","spid":"s","pwd":"p",
                         "commercialCode":"0062","contentTemplate":"%s","expireTime":300,"timeout":10000}
                        """.formatted(SmsConfigLoader.DEFAULT_CONTENT_TEMPLATE)
                )
        ));
        GatewaySmsSender sender = new GatewaySmsSender(new SmsConfigLoader(repository, new ObjectMapper()), new ObjectMapper());
        SmsSendResult result = sender.send("81234567890", "654321");
        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).startsWith("mock-");
    }

    @Test
    void failsWhenGatewayCredentialsMissing() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(any())).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        "smsConf",
                        "{\"enableSms\":true,\"url\":\"\",\"spid\":\"\",\"pwd\":\"\",\"commercialCode\":\"0062\",\"expireTime\":300,\"timeout\":10000}"
                )
        ));
        GatewaySmsSender sender = new GatewaySmsSender(new SmsConfigLoader(repository, new ObjectMapper()), new ObjectMapper());
        SmsSendResult result = sender.send("81234567890", "654321");
        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo("CONFIG_MISSING");
    }
}
