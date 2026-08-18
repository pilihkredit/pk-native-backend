package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.tracking.LenderTrackingEvent;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ApiPartnerTrackingAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsTrackingEventToConfiguredEndpoint() throws Exception {
        List<String> bodies = new ArrayList<>();
        server = startServer(204, "", bodies);
        RecordingInteractionLogRepository logs = new RecordingInteractionLogRepository();
        ApiPartnerTrackingAdapter adapter = new ApiPartnerTrackingAdapter(properties(server), logs, objectMapper, null);

        adapter.submitEvents(List.of(sampleEvent()));

        assertThat(bodies).hasSize(1);
        JsonNode body = objectMapper.readTree(bodies.getFirst());
        assertThat(body.get("timestamp").asLong()).isEqualTo(1_750_500_000_000L);
        assertThat(body.get("uid").asText()).isEqualTo("partner-1");
        assertThat(body.get("eventType").asText()).isEqualTo("loan_page_enter");
        assertThat(body.get("clientNo").asText()).isEqualTo("device-1");
        assertThat(body.get("ip").asText()).isEqualTo("192.168.1.10");
        assertThat(body.get("datetime").asText()).isEqualTo("2025-06-21 17:00:00");
        assertThat(body.get("extend").get("applyId").asText()).isEqualTo("APPLY-1");
        assertThat(logs.logs).hasSize(1);
        assertThat(logs.logs.getFirst().isSuccess()).isTrue();
    }

    @Test
    void throwsServiceUnavailableWhenLenderReturnsNon2xx() throws Exception {
        server = startServer(500, "error", new ArrayList<>());
        RecordingInteractionLogRepository logs = new RecordingInteractionLogRepository();
        ApiPartnerTrackingAdapter adapter = new ApiPartnerTrackingAdapter(properties(server), logs, objectMapper, null);

        assertThatThrownBy(() -> adapter.submitEvents(List.of(sampleEvent())))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);

        assertThat(logs.logs).hasSize(1);
        assertThat(logs.logs.getFirst().isSuccess()).isFalse();
    }

    private static HttpServer startServer(int status, String responseBody, List<String> bodies) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            bodies.add(body);
            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        httpServer.start();
        return httpServer;
    }

    private static ApiPartnerProperties properties(HttpServer server) {
        ApiPartnerProperties properties = new ApiPartnerProperties();
        properties.setTrackingUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setConnectTimeoutMs(1_000);
        properties.setReadTimeoutMs(1_000);
        properties.logging().setEnabled(false);
        return properties;
    }

    private static LenderTrackingEvent sampleEvent() {
        return new LenderTrackingEvent(
                1_750_500_000_000L,
                "partner-1",
                "loan_page_enter",
                "/loan",
                Map.of("applyId", "APPLY-1"),
                "trace-1",
                "device-1",
                "Apple",
                "iPhone 7",
                "phone",
                "iOS",
                "9.2.1",
                "com.pk.app",
                "1.0.0",
                "1.0.0",
                "Chrome",
                "126.0.0",
                null,
                null,
                "idfv-1",
                "idfa-1",
                "192.168.1.10",
                "2025-06-21 17:00:00"
        );
    }

    private static final class RecordingInteractionLogRepository implements LenderInteractionLogRepository {
        private final List<LenderInteractionLog> logs = new ArrayList<>();

        @Override
        public long insert(LenderInteractionLog log) {
            logs.add(log);
            log.setId((long) logs.size());
            return log.getId();
        }
    }
}
