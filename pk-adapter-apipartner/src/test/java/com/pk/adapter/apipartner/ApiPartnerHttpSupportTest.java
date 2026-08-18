package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiPartnerHttpSupportTest {
    @Test
    void extractsMobileNoFromNestedJson() {
        assertThat(ApiPartnerHttpSupport.extractMobileNo("""
                {"partnerUserId":"U1","userInfo":{"mobileNo":"81234567890"}}
                """)).isEqualTo("81234567890");
        assertThat(ApiPartnerHttpSupport.extractMobileNo("""
                {"mobileNo":"801234567"}
                """)).isEqualTo("801234567");
        assertThat(ApiPartnerHttpSupport.extractMobileNo("{\"partnerUserId\":\"U1\"}")).isNull();
    }

    @Test
    void previewsSecretsButKeepsFaceBase64Full() {
        String longSecret = "s".repeat(120);
        String longToken = "t".repeat(120);
        String longImage = "i".repeat(120);
        String payload = """
                {"clientId":"id","clientSecret":"%s","data":{"accessToken":"%s"},"identity":{"faceBase64":"%s","name":"Alice"}}
                """.formatted(longSecret, longToken, longImage);

        String redacted = ApiPartnerHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("\"clientSecret\":\"" + "s".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"accessToken\":\"" + "t".repeat(100) + "...[truncated]\"");
        assertThat(redacted).contains("\"faceBase64\":\"" + longImage + "\"");
        assertThat(redacted).doesNotContain("faceBase64\":\"" + "i".repeat(100) + "...[truncated]");
        assertThat(redacted).contains("\"name\":\"Alice\"");
    }

    @Test
    void formatLogBodyUnlimitedWhenMaxBodyBytesNonPositive() {
        String body = "x".repeat(20);

        assertThat(ApiPartnerHttpSupport.formatLogBody(body, 0)).isEqualTo(body);
        assertThat(ApiPartnerHttpSupport.formatLogBody(body, -1)).isEqualTo(body);
    }

    @Test
    void preservesLenderResponseMsgField() {
        String payload = """
                {"code":"A000445","msg":"advanceAi OCR raw payload format error","data":null,"success":false}
                """;

        String redacted = ApiPartnerHttpSupport.redactSensitiveJson(payload);

        assertThat(redacted).contains("advanceAi OCR raw payload format error");
        assertThat(redacted).doesNotContain("[redacted]");
    }

    @Test
    void formatsTransportFailureMessage() {
        assertThat(ApiPartnerHttpSupport.formatTransportFailure(new java.net.http.HttpTimeoutException("request timed out")))
                .isEqualTo("HttpTimeoutException: request timed out");
    }

    @Test
    void appliesTransportFailureToEmptyResponseOutcome() {
        ApiPartnerHttpSupport.InteractionOutcome outcome = new ApiPartnerHttpSupport.InteractionOutcome();
        outcome.transportFailure = "HttpTimeoutException: request timed out";

        ApiPartnerHttpSupport.applyTransportFailureForLog(outcome);

        assertThat(outcome.responseCode).isEqualTo("TRANSPORT_ERROR");
        assertThat(outcome.responseMsg).contains("HttpTimeoutException");
    }

    @Test
    void truncatesStorageRefWhenExceedingLimit() {
        String body = "x".repeat(ApiPartnerHttpSupport.STORAGE_REF_MAX_CHARS + 10);

        String truncated = ApiPartnerHttpSupport.truncate(body);

        assertThat(truncated).hasSize(ApiPartnerHttpSupport.STORAGE_REF_MAX_CHARS);
    }

    @Test
    void truncatesLogBodyWhenExceedingLimit() {
        String body = "x".repeat(20);

        String formatted = ApiPartnerHttpSupport.formatLogBody(body, 10);

        assertThat(formatted).isEqualTo("xxxxxxxxxx...[truncated 10 bytes]");
    }

    @Test
    void ensureSuccessThrowsWithLenderMessage() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode envelope = mapper.readTree("""
                {"code":"A000001","msg":"Invalid request parameters","data":null}
                """);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ApiPartnerHttpSupport.ensureSuccess(envelope))
                .isInstanceOf(com.pk.core.api.ApiException.class)
                .satisfies(throwable -> {
                    com.pk.core.api.ApiException exception = (com.pk.core.api.ApiException) throwable;
                    assertThat(exception.detail()).isEqualTo("Invalid request parameters");
                });
    }
}
