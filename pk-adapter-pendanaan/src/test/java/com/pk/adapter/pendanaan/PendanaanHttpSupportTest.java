package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PendanaanHttpSupportTest {
    @Test
    void redactsClientSecretAndAccessToken() {
        String payload = """
                {"clientId":"id","clientSecret":"secret","data":{"accessToken":"jwt-token"}}
                """;

        String redacted = PendanaanHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("\"clientSecret\":\"***\"");
        assertThat(redacted).doesNotContain("secret");
        assertThat(redacted).contains("\"accessToken\":\"***\"");
        assertThat(redacted).doesNotContain("jwt-token");
    }

    @Test
    void truncatesLogBodyWhenExceedingLimit() {
        String body = "x".repeat(20);

        String formatted = PendanaanHttpSupport.formatLogBody(body, 10);

        assertThat(formatted).isEqualTo("xxxxxxxxxx...[truncated 10 bytes]");
    }
}
