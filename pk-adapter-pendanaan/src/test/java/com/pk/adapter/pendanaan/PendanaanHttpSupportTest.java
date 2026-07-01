package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PendanaanHttpSupportTest {
    @Test
    void previewsClientSecretAccessTokenAndImageFields() {
        String longSecret = "s".repeat(120);
        String longToken = "t".repeat(120);
        String longImage = "i".repeat(120);
        String payload = """
                {"clientId":"id","clientSecret":"%s","data":{"accessToken":"%s"},"identity":{"faceBase64":"%s","name":"Alice"}}
                """.formatted(longSecret, longToken, longImage);

        String redacted = PendanaanHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("\"clientSecret\":\"" + "s".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"accessToken\":\"" + "t".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"faceBase64\":\"" + "i".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"name\":\"Alice\"");
    }

    @Test
    void formatsTransportFailureMessage() {
        assertThat(PendanaanHttpSupport.formatTransportFailure(new java.net.http.HttpTimeoutException("request timed out")))
                .isEqualTo("HttpTimeoutException: request timed out");
    }

    @Test
    void appliesTransportFailureToEmptyResponseOutcome() {
        PendanaanHttpSupport.InteractionOutcome outcome = new PendanaanHttpSupport.InteractionOutcome();
        outcome.transportFailure = "HttpTimeoutException: request timed out";

        PendanaanHttpSupport.applyTransportFailureForLog(outcome);

        assertThat(outcome.responseCode).isEqualTo("TRANSPORT_ERROR");
        assertThat(outcome.responseMsg).contains("HttpTimeoutException");
    }

    @Test
    void truncatesLogBodyWhenExceedingLimit() {
        String body = "x".repeat(20);

        String formatted = PendanaanHttpSupport.formatLogBody(body, 10);

        assertThat(formatted).isEqualTo("xxxxxxxxxx...[truncated 10 bytes]");
    }
}
