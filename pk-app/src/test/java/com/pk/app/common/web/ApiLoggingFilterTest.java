package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.pk.core.api.ApiCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiLoggingFilterTest {
    private ListAppender<ILoggingEvent> logAppender;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiLoggingFilter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        ApiProperties apiProperties = new ApiProperties();
        ApiLoggingProperties loggingProperties = new ApiLoggingProperties();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(new ApiLoggingFilter(apiProperties, loggingProperties))
                .build();
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiLoggingFilter.class);
        logger.detachAppender(logAppender);
    }

    @Test
    void logsRequestAndResponseBodiesForApiPaths() throws Exception {
        mockMvc.perform(
                        post("/api/v1/echo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"alice\"}")
                                .header("X-Trace-Id", "trace-echo")
                )
                .andExpect(status().isOk());

        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("API request")
                        && message.contains("path=/api/v1/echo")
                        && message.contains("traceId=trace-echo")
                        && message.contains("body={\"name\":\"alice\"}"))
                .anyMatch(message -> message.contains("API response")
                        && message.contains("status=200")
                        && message.contains("body={\"code\":\"000000\""));
    }

    @Test
    void skipsNonApiPaths() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Trace-Id", "trace-health"))
                .andExpect(status().isNotFound());

        assertThat(logAppender.list).isEmpty();
    }

    @Test
    void logsQueryStringForGetRequests() throws Exception {
        mockMvc.perform(get("/api/v1/greet").param("name", "bob").header("X-Trace-Id", "trace-get"))
                .andExpect(status().isOk());

        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("API request")
                        && message.contains("method=GET")
                        && message.contains("query=name=bob"));
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
