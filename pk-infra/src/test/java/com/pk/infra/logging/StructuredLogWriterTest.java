package com.pk.infra.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.logging.StructuredLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StructuredLogWriterTest {
    private StructuredLogWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LoggingRuntimeContext runtimeContext = Mockito.mock(LoggingRuntimeContext.class);
        when(runtimeContext.service()).thenReturn("pk-app");
        when(runtimeContext.environment()).thenReturn("test");
        writer = new StructuredLogWriter(objectMapper, runtimeContext);
    }

    @Test
    void buildsPlatformJsonSchema() throws Exception {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("pk.structured");
        ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender =
                new ch.qos.logback.core.read.ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        writer.log(StructuredLogEntry.builder("INFO", "api.access")
                .traceId("trace-1")
                .uri("/api/v1/demo")
                .method("GET")
                .status(200)
                .durationMs(120L)
                .code("000000")
                .build());

        assertThat(appender.list).hasSize(1);
        JsonNode json = objectMapper.readTree(appender.list.get(0).getFormattedMessage());
        assertThat(json.get("service").asText()).isEqualTo("pk-app");
        assertThat(json.get("environment").asText()).isEqualTo("test");
        assertThat(json.get("traceId").asText()).isEqualTo("trace-1");
        assertThat(json.get("uri").asText()).isEqualTo("/api/v1/demo");
        assertThat(json.get("status").asInt()).isEqualTo(200);
        assertThat(json.get("durationMs").asLong()).isEqualTo(120L);
        assertThat(json.get("code").asText()).isEqualTo("000000");
    }
}
