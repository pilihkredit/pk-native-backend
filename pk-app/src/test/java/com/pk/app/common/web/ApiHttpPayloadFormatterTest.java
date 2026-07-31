package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiHttpPayloadFormatterTest {
    @Test
    void returnsEmptyStringForEmptyBody() {
        assertThat(ApiHttpPayloadFormatter.formatBody(new byte[0], "application/json", 1024)).isEmpty();
    }

    @Test
    void truncatesLargeTextBodies() {
        byte[] body = "abcdefghij".repeat(20).getBytes();

        String formatted = ApiHttpPayloadFormatter.formatBody(body, "application/json", 50);

        assertThat(formatted).contains("...[truncated");
        assertThat(formatted.length()).isLessThan(body.length);
    }

    @Test
    void describesBinaryPayloadsWithoutDumpingBytes() {
        byte[] body = new byte[] {0x00, 0x01, 0x02};

        String formatted = ApiHttpPayloadFormatter.formatBody(body, "application/octet-stream", 1024);

        assertThat(formatted).isEqualTo("[binary contentType=application/octet-stream length=3]");
    }

    @Test
    void formatsRequestHeadersAndRedactsAuthorization() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer secret-token");
        request.addHeader("X-Device-No", "device-1");
        request.addHeader("X-App-Version", "1.0.0");

        Map<String, String> headers = ApiHttpPayloadFormatter.formatHeaders(request);

        assertThat(headers)
                .containsEntry("X-Device-No", "device-1")
                .containsEntry("X-App-Version", "1.0.0")
                .containsEntry("Authorization", "Bearer ***");
        assertThat(headers.values()).noneMatch(value -> value.contains("secret-token"));
    }
}
