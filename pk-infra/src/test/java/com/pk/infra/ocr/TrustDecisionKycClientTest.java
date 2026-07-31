package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TrustDecisionKycClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void obtainsSdkLicenseAndRetrievesInteractiveLivenessResult() throws Exception {
        AtomicReference<String> licenseRequest = new AtomicReference<>();
        AtomicReference<String> resultRequest = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/license", exchange -> {
            licenseRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, """
                    {"code":200,"message":"success","sequence_id":"license-seq",
                     "license":"license-value","expiry_timestamp":1785147934}
                    """);
        });
        server.createContext("/result", exchange -> {
            resultRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, """
                    {"code":200,"message":"success","sequence_id":"result-seq",
                     "image":"%s","device_risk_tag":[],"device_risk_level":0}
                    """.formatted(Base64.getEncoder().encodeToString(new byte[] {7, 8})));
        });
        server.start();

        TrustDecisionProperties properties = propertiesForSdkServer();
        TrustDecisionKycClient client = client(properties);

        var license = client.obtainLivenessLicense(600);
        var result = client.retrieveLivenessResult("sdk-live-id");

        assertThat(license.license()).isEqualTo("license-value");
        assertThat(license.expiryTimestamp()).isEqualTo(1785147934L);
        assertThat(licenseRequest.get()).contains("\"session_duration\":600");
        assertThat(result.result()).isEqualTo("pass");
        assertThat(result.faceImage()).containsExactly(7, 8);
        assertThat(resultRequest.get()).contains("\"liveness_id\":\"sdk-live-id\"");
    }

    @Test
    void treatsSdkFakePersonCodeAsCompletedResult() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/result", exchange -> respond(exchange, """
                {"code":12201,"message":"fail","sequence_id":"result-seq",
                 "image":"%s","device_risk_tag":[],"device_risk_level":0}
                """.formatted(Base64.getEncoder().encodeToString(new byte[] {9}))));
        server.start();

        var result = client(propertiesForSdkServer()).retrieveLivenessResult("sdk-live-id");

        assertThat(result.result()).isEqualTo("fail");
        assertThat(result.sequenceId()).isEqualTo("result-seq");
    }

    private TrustDecisionProperties propertiesForSdkServer() {
        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setPartnerCode("partner-code");
        properties.setPartnerKey("partner-key");
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        properties.setLivenessLicenseUrl(baseUrl + "/license");
        properties.setLivenessResultUrl(baseUrl + "/result");
        return properties;
    }

    private static TrustDecisionKycClient client(TrustDecisionProperties properties) {
        OcrVendorCallLogWriter logWriter = mock(OcrVendorCallLogWriter.class);
        when(logWriter.write(any())).thenReturn(7L);
        OcrSensitiveJsonSupport sensitiveJsonSupport = mock(OcrSensitiveJsonSupport.class);
        when(sensitiveJsonSupport.sanitizeForStorage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new TrustDecisionKycClient(properties, new ObjectMapper(), logWriter, sensitiveJsonSupport);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws java.io.IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
