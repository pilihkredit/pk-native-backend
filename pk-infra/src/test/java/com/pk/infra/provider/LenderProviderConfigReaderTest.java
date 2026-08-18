package com.pk.infra.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class LenderProviderConfigReaderTest {
    @Test
    void mapsProviderRowToSpringProperties() {
        LenderProviderConfigRecord record = new LenderProviderConfigRecord(
                "apipartner",
                "ApiPartner Test",
                "http://gateway.test.ptnadmin.com/ktaid",
                "https://api.example.com/api/v1",
                """
                {"mode":"http","appName":"KtaKilatPlus"}
                """,
                "{\"appName\":\"PilihKredit\"}",
                "client-id",
                "client-secret",
                "callback-client",
                "callback-secret"
        );

        Map<String, Object> properties = LenderProviderConfigReader.toSpringProperties(record);

        assertThat(properties)
                .containsEntry("pk.lender.config.provider-name", "ApiPartner Test")
                .containsEntry("pk.lender.apipartner.mode", "http")
                .containsEntry("pk.lender.apipartner.base-url", "http://gateway.test.ptnadmin.com/ktaid")
                .containsEntry("pk.lender.apipartner.client-id", "client-id")
                .containsEntry("pk.lender.apipartner.client-secret", "client-secret")
                .containsEntry("pk.lender.apipartner.app-name", "PilihKredit")
                .containsEntry("pk.callback.oauth.client-id", "callback-client")
                .containsEntry("pk.callback.oauth.client-secret", "callback-secret");
    }

    @Test
    void defaultsModeToHttpWhenConfigJsonOmitsMode() {
        LenderProviderConfigRecord record = new LenderProviderConfigRecord(
                "apipartner",
                "ApiPartner Test",
                "http://example.com",
                "https://api.example.com/api/v1",
                "{\"app_name\":\"KtaKilatPlus\"}",
                null,
                "client-id",
                "client-secret",
                null,
                null
        );

        Map<String, Object> properties = LenderProviderConfigReader.toSpringProperties(record);

        assertThat(properties)
                .containsEntry("pk.lender.apipartner.mode", "http")
                .doesNotContainKey("pk.lender.apipartner.app-name")
                .doesNotContainKey("pk.callback.oauth.client-id");
    }
}
