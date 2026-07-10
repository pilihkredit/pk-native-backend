package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.logging.StructuredLogEntry;
import com.pk.infra.logging.StructuredLogWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiLoggingFilterTest {
    private final StructuredLogWriter structuredLogWriter = mock(StructuredLogWriter.class);
    private final MockMvc mockMvc;

    ApiLoggingFilterTest() {
        ApiProperties apiProperties = new ApiProperties();
        ApiLoggingProperties loggingProperties = new ApiLoggingProperties();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(new ApiLoggingFilter(
                        apiProperties,
                        loggingProperties,
                        structuredLogWriter,
                        new ObjectMapper()
                ))
                .build();
    }

    @Test
    void logsStructuredAccessEventForApiPaths() throws Exception {
        mockMvc.perform(
                        post("/api/v1/echo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"alice\"}")
                                .header("X-Trace-Id", "trace-echo")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<StructuredLogEntry> captor = ArgumentCaptor.forClass(StructuredLogEntry.class);
        verify(structuredLogWriter).log(captor.capture());
        StructuredLogEntry entry = captor.getValue();
        assertThat(entry.message()).isEqualTo("api.access");
        assertThat(entry.traceId()).isEqualTo("trace-echo");
        assertThat(entry.uri()).isEqualTo("/api/v1/echo");
        assertThat(entry.method()).isEqualTo("POST");
        assertThat(entry.status()).isEqualTo(200);
        assertThat(entry.code()).isEqualTo("000000");
        assertThat(entry.durationMs()).isNotNull();
    }

    @Test
    void skipsNonApiPaths() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Trace-Id", "trace-health"))
                .andExpect(status().isNotFound());

        verify(structuredLogWriter, org.mockito.Mockito.never()).log(any());
    }

    @Test
    void logsQueryStringForGetRequests() throws Exception {
        mockMvc.perform(get("/api/v1/greet").param("name", "bob").header("X-Trace-Id", "trace-get"))
                .andExpect(status().isOk());

        ArgumentCaptor<StructuredLogEntry> captor = ArgumentCaptor.forClass(StructuredLogEntry.class);
        verify(structuredLogWriter).log(captor.capture());
        assertThat(captor.getValue().extra()).containsEntry("query", "name=bob");
    }

    @RestController
    private static class TestController {
        @PostMapping("/api/v1/echo")
        ApiResponse<EchoResponse> echo(@RequestBody EchoRequest request) {
            return ApiResponse.success(new EchoResponse(request.name()), "trace");
        }

        @GetMapping("/api/v1/greet")
        ApiResponse<String> greet() {
            return ApiResponse.of(ApiCode.SUCCESS, "hello", "trace");
        }
    }

    private record EchoRequest(String name) {
    }

    private record EchoResponse(String name) {
    }
}
