package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PendanaanHttpSupportTest {
    @Test
    void extractsMobileNoFromNestedJson() {
        assertThat(PendanaanHttpSupport.extractMobileNo("""
                {"partnerUserId":"U1","userInfo":{"mobileNo":"81234567890"}}
                """)).isEqualTo("81234567890");
        assertThat(PendanaanHttpSupport.extractMobileNo("""
                {"mobileNo":"801234567"}
                """)).isEqualTo("801234567");
        assertThat(PendanaanHttpSupport.extractMobileNo("{\"partnerUserId\":\"U1\"}")).isNull();
    }

    @Test
    void previewsSecretsButKeepsFaceBase64Full() {
        String longSecret = "s".repeat(120);
        String longToken = "t".repeat(120);
        String longImage = "i".repeat(120);
        String payload = """
                {"clientId":"id","clientSecret":"%s","data":{"accessToken":"%s"},"identity":{"faceBase64":"%s","name":"Alice"}}
                """.formatted(longSecret, longToken, longImage);

        String redacted = PendanaanHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("\"clientSecret\":\"" + "s".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"accessToken\":\"" + "t".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"faceBase64\":\"" + longImage + "\"");
        assertThat(redacted).doesNotContain("faceBase64\":\"" + "i".repeat(100) + "...[truncated]");
        assertThat(redacted).contains("\"name\":\"Alice\"");
    }

    @Test
    void formatLogBodyUnlimitedWhenMaxBodyBytesNonPositive() {
        String body = "x".repeat(20);

        assertThat(PendanaanHttpSupport.formatLogBody(body, 0)).isEqualTo(body);
        assertThat(PendanaanHttpSupport.formatLogBody(body, -1)).isEqualTo(body);
    }

    @Test
    void preservesLenderResponseMsgField() {
        String payload = """
                {"code":"A000445","msg":"advanceAi OCR原始报文格式错误","data":null,"success":false}
                """;

        String redacted = PendanaanHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("advanceAi OCR原始报文格式错误");
        assertThat(redacted).doesNotContain("[redacted]");
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
    void truncatesStorageRefWhenExceedingLimit() {
        String body = "x".repeat(PendanaanHttpSupport.STORAGE_REF_MAX_CHARS + 10);

        String truncated = PendanaanHttpSupport.truncate(body);

        assertThat(truncated).hasSize(PendanaanHttpSupport.STORAGE_REF_MAX_CHARS);
    }

    @Test
    void truncatesLogBodyWhenExceedingLimit() {
        String body = "x".repeat(20);

        String formatted = PendanaanHttpSupport.formatLogBody(body, 10);

        assertThat(formatted).isEqualTo("xxxxxxxxxx...[truncated 10 bytes]");
    }

    @Test
    void ensureSuccessThrowsWithLenderMessage() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode envelope = mapper.readTree("""
                {"code":"A000001","msg":"请求参数错误","data":null}
                """);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> PendanaanHttpSupport.ensureSuccess(envelope))
                .isInstanceOf(com.pk.core.api.ApiException.class)
                .satisfies(throwable -> {
                    com.pk.core.api.ApiException exception = (com.pk.core.api.ApiException) throwable;
                    assertThat(exception.detail()).isEqualTo("请求参数错误");
                });
    }
}
