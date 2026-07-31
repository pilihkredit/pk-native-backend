package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TrustDecisionCredentialLeakTest {
    private static final String TEST_KEY = "test-partner-key-value";
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void rejectsEnabledConfigurationWithoutCredentials() {
        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setEnabled(true);

        assertThatThrownBy(properties::validateEnabledSettings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("partner-code")
                .hasMessageContaining("partner-key");
    }

    @Test
    void excludesCredentialsFromTechnicalFailureAndAudit() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/license", exchange -> {
            byte[] response = "service unavailable".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(503, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setPartnerCode("test-partner-code");
        properties.setPartnerKey(TEST_KEY);
        properties.setLivenessLicenseUrl("http://localhost:" + server.getAddress().getPort() + "/license");
        OcrVendorCallLogWriter logWriter = mock(OcrVendorCallLogWriter.class);
        when(logWriter.write(any())).thenReturn(1L);
        OcrSensitiveJsonSupport sensitiveJsonSupport = mock(OcrSensitiveJsonSupport.class);
        when(sensitiveJsonSupport.sanitizeForStorage(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TrustDecisionKycClient client = new TrustDecisionKycClient(
                properties, new ObjectMapper(), logWriter, sensitiveJsonSupport);

        assertThatThrownBy(() -> client.obtainLivenessLicense(600))
                .isInstanceOf(ApiException.class)
                .hasMessageNotContaining(TEST_KEY);

        ArgumentCaptor<OcrVendorCallLogWriter.OcrVendorCallLogEntry> captor =
                ArgumentCaptor.forClass(OcrVendorCallLogWriter.OcrVendorCallLogEntry.class);
        verify(logWriter).write(captor.capture());
        var entry = captor.getValue();
        assertThat(entry.endpoint()).doesNotContain(TEST_KEY).doesNotContain("partner_key");
        assertThat(entry.requestJson()).doesNotContain(TEST_KEY);
        assertThat(entry.responseJson()).doesNotContain(TEST_KEY);
        assertThat(entry.vendorMessage()).isNull();
    }
}
