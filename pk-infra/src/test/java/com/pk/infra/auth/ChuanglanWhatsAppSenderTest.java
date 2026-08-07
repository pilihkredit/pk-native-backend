package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.auth.SmsSendResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChuanglanWhatsAppSenderTest {
    @Test
    void buildPayloadUsesTemplateGroupIdAndOmitsTemplateName() {
        WhatsAppConfigLoader.WhatsAppConf conf = sampleConf(true, "group_abc");

        Map<String, Object> payload = ChuanglanWhatsAppSender.buildPayload(conf, "8123456789", "654321");

        assertThat(payload.get("templateGroupId")).isEqualTo("group_abc");
        assertThat(payload).doesNotContainKey("templateName");
        assertThat(payload.get("messageType")).isEqualTo("template");
        assertThat(payload.get("bodyParams")).isEqualTo(List.of("654321"));
        assertThat(payload.get("recipientNumber")).isEqualTo("628123456789");
    }

    @Test
    void mocksWhenWhatsAppDisabled() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(WhatsAppConfigLoader.CONF_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        WhatsAppConfigLoader.CONF_KEY,
                        """
                        {
                          "enableWhatsApp": false,
                          "url": "https://example.com/v3",
                          "templateGroupId": "group_x"
                        }
                        """
                )
        ));
        ChuanglanWhatsAppSender sender =
                new ChuanglanWhatsAppSender(new WhatsAppConfigLoader(repository, new ObjectMapper()), new ObjectMapper());

        SmsSendResult result = sender.send("8123456789", "123456");

        assertThat(result.success()).isTrue();
        assertThat(result.providerMessageId()).startsWith("mock-");
    }

    private static WhatsAppConfigLoader.WhatsAppConf sampleConf(boolean enabled, String templateGroupId) {
        return new WhatsAppConfigLoader.WhatsAppConf(
                enabled,
                "https://api.innopaas.com/api/whatsapp/v3",
                "app-key",
                "authorization",
                "waba-1",
                "628111111111",
                templateGroupId,
                "id",
                "62",
                Duration.ofSeconds(10),
                Duration.ofSeconds(60),
                Duration.ofSeconds(300),
                "1234",
                List.of()
        );
    }
}
