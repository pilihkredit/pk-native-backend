package com.pk.infra.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class LenderProviderConfigReaderTest {
    @Test
    void mapsProviderRowToSpringProperties() {
        LenderProviderConfigRecord record = new LenderProviderConfigRecord(
                "pendanaan",
                "Pendanaan Test",
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
                .containsEntry("pk.lender.config.provider-name", "Pendanaan Test")
                .containsEntry("pk.lender.pendanaan.mode", "http")
                .containsEntry("pk.lender.pendanaan.base-url", "http://gateway.test.ptnadmin.com/ktaid")
                .containsEntry("pk.lender.pendanaan.client-id", "client-id")
                .containsEntry("pk.lender.pendanaan.client-secret", "client-secret")
                .containsEntry("pk.lender.pendanaan.app-name", "PilihKredit")
                .containsEntry("pk.callback.oauth.client-id", "callback-client")
                .containsEntry("pk.callback.oauth.client-secret", "callback-secret");
    }

    @Test
    void defaultsModeToHttpWhenConfigJsonOmitsMode() {
        LenderProviderConfigRecord record = new LenderProviderConfigRecord(
                "pendanaan",
                "Pendanaan Test",
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
                .containsEntry("pk.lender.pendanaan.mode", "http")
                .doesNotContainKey("pk.lender.pendanaan.app-name")
                .doesNotContainKey("pk.callback.oauth.client-id");
    }
}
