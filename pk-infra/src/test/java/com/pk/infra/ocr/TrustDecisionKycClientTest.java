package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TrustDecisionKycClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void treatsLivenessFailAsCompletedCallAndSanitizesAuditEndpoint() throws Exception {
        AtomicReference<String> query = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/liveness", exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            byte[] response = """
                    {"code":200,"message":"success","sequence_id":"live-seq","result":"fail","score":0.41}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setPartnerCode("partner-code");
        properties.setPartnerKey("partner-key");
        properties.setLivenessUrl("http://localhost:" + server.getAddress().getPort() + "/liveness");
        OcrVendorCallLogWriter logWriter = mock(OcrVendorCallLogWriter.class);
        when(logWriter.write(any())).thenReturn(7L);
        OcrSensitiveJsonSupport sensitiveJsonSupport = mock(OcrSensitiveJsonSupport.class);
        when(sensitiveJsonSupport.sanitizeForStorage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        TrustDecisionKycClient client = new TrustDecisionKycClient(
                properties,
                new ObjectMapper(),
                logWriter,
                sensitiveJsonSupport
        );

        var result = client.checkLiveness(new byte[] {1, 2, 3});

        assertThat(result.result()).isEqualTo("fail");
        assertThat(result.score()).isEqualTo(0.41D);
        assertThat(query.get()).contains("partner_code=partner-code", "partner_key=partner-key");
        ArgumentCaptor<OcrVendorCallLogWriter.OcrVendorCallLogEntry> captor =
                ArgumentCaptor.forClass(OcrVendorCallLogWriter.OcrVendorCallLogEntry.class);
        verify(logWriter).write(captor.capture());
        assertThat(captor.getValue().channel()).isEqualTo("trustDecision");
        assertThat(captor.getValue().endpoint()).doesNotContain("?");
        assertThat(captor.getValue().vendorCode()).isEqualTo("200");
    }
}
