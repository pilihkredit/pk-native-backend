package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
}
