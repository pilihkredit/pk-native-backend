package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OcrProviderConfigLoaderTest {
    @Test
    void loadsAdvanceAiConfigurationOnEveryRequest() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("advanceAiConf"))
                .thenReturn(Optional.of(record("advanceAiConf", """
                        {"enabled":true,"accessKey":"access-one","secretKey":"secret-one"}
                        """)))
                .thenReturn(Optional.of(record("advanceAiConf", """
                        {"enabled":true,"accessKey":"access-two","secretKey":"secret-two"}
                        """)));
        OcrProviderConfigLoader loader = new OcrProviderConfigLoader(repository, new ObjectMapper());

        assertThat(loader.loadAdvanceAi().accessKey()).isEqualTo("access-one");
        assertThat(loader.loadAdvanceAi().accessKey()).isEqualTo("access-two");
    }

    @Test
    void loadsEnabledTrustDecisionConfiguration() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("trustDecisionConf")).thenReturn(Optional.of(record(
                "trustDecisionConf",
                """
                        {"enabled":true,"partnerCode":"partner-code","partnerKey":"partner-key"}
                        """)));
        OcrProviderConfigLoader loader = new OcrProviderConfigLoader(repository, new ObjectMapper());

        TrustDecisionProperties properties = loader.loadTrustDecision();

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.partnerCode()).isEqualTo("partner-code");
        assertThat(properties.partnerKey()).isEqualTo("partner-key");
        assertThat(properties.ocrUrl()).isEqualTo("https://id.apitd.net/verification/kyc/ocr/v1");
    }

    @Test
    void rejectsMissingOrDisabledProviderConfiguration() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey("advanceAiConf")).thenReturn(Optional.empty());
        when(repository.findByKey("trustDecisionConf")).thenReturn(Optional.of(record(
                "trustDecisionConf", "{\"enabled\":false}")));
        OcrProviderConfigLoader loader = new OcrProviderConfigLoader(repository, new ObjectMapper());

        assertThatThrownBy(loader::loadAdvanceAi).isInstanceOf(ApiException.class);
        assertThatThrownBy(loader::loadTrustDecision).isInstanceOf(ApiException.class);
    }

    private static AppConfigRepository.AppConfigRecord record(String key, String value) {
        return new AppConfigRepository.AppConfigRecord(1L, key, value);
    }
}
