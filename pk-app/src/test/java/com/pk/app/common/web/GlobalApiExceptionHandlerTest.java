package com.pk.app.common.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.logging.StructuredLogEntry;
import com.pk.infra.logging.StructuredLogWriter;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

class GlobalApiExceptionHandlerTest {
    private final StructuredLogWriter structuredLogWriter = mock(StructuredLogWriter.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalApiExceptionHandler(structuredLogWriter))
            .build();

    @Test
    void mapsApiExceptionDetailToClientMessage() throws Exception {
        mockMvc.perform(get("/test/api-exception-with-detail").header("X-Trace-Id", "trace-detail"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("K000001"))
                .andExpect(jsonPath("$.msg").value("device.deviceNo does not match header X-Device-No"))
                .andExpect(jsonPath("$.traceId").value("trace-detail"));
    }

    @Test
    void mapsApiExceptionToWrappedFailure() throws Exception {
        mockMvc.perform(get("/test/api-exception").header("X-Trace-Id", "trace-api"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("K000145"))
                .andExpect(jsonPath("$.msg").value("Invalid loan amount"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.traceId").value("trace-api"));
    }

    @Test
    void mapsAnnotatedApiExceptionToAnnotatedHttpStatus() throws Exception {
        mockMvc.perform(get("/test/service-unavailable").header("X-Trace-Id", "trace-service"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("999998"))
                .andExpect(jsonPath("$.msg").value("Service unavailable"))
                .andExpect(jsonPath("$.traceId").value("trace-service"));

        verify(structuredLogWriter).logError(
                org.mockito.ArgumentMatchers.eq("api.error"),
                org.mockito.ArgumentMatchers.eq("trace-service"),
                org.mockito.ArgumentMatchers.eq("/test/service-unavailable"),
                org.mockito.ArgumentMatchers.eq("GET"),
                any(),
                any()
        );
    }

    @Test
    void logsValidationExceptionDetails() throws Exception {
        mockMvc.perform(get("/test/validated").header("X-Trace-Id", "trace-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("K000001"))
                .andExpect(jsonPath("$.msg").value("name: is required"))
                .andExpect(jsonPath("$.traceId").value("trace-validation"));
    }

    @Test
    void mapsUnauthorizedApiExceptionTo401() throws Exception {
        mockMvc.perform(get("/test/unauthorized").header("X-Trace-Id", "trace-unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("K000012"))
                .andExpect(jsonPath("$.msg").value("Unauthorized request"))
                .andExpect(jsonPath("$.traceId").value("trace-unauthorized"));
    }

    @Test
    void mapsUnknownExceptionToInternalServerError() throws Exception {
        mockMvc.perform(get("/test/unknown").header("X-Trace-Id", "trace-unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("999999"))
                .andExpect(jsonPath("$.msg").value("Internal server error"))
                .andExpect(jsonPath("$.traceId").value("trace-unknown"));

        verify(structuredLogWriter).logError(
                org.mockito.ArgumentMatchers.eq("api.error"),
                org.mockito.ArgumentMatchers.eq("trace-unknown"),
                org.mockito.ArgumentMatchers.eq("/test/unknown"),
                org.mockito.ArgumentMatchers.eq("GET"),
                any(),
                any()
        );
    }

    @Validated
    @RestController
    private static class TestController {
        @GetMapping("/test/api-exception-with-detail")
        ApiResponse<Void> apiExceptionWithDetail() {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device.deviceNo does not match header X-Device-No"
            );
        }

        @GetMapping("/test/api-exception")
        ApiResponse<Void> apiException() {
            throw new ApiException(ApiCode.INVALID_LOAN_AMOUNT);
        }

        @GetMapping("/test/service-unavailable")
        ApiResponse<Void> serviceUnavailable() {
            throw new ServiceUnavailableException();
        }

        @GetMapping("/test/validated")
        ApiResponse<String> validated(@RequestParam("name") @NotBlank String name) {
            return ApiResponse.success(name, "trace");
        }

        @GetMapping("/test/unauthorized")
        ApiResponse<Void> unauthorized() {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }

        @GetMapping("/test/unknown")
        ApiResponse<Void> unknown() {
            throw new IllegalStateException("Unexpected failure");
        }
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    private static final class ServiceUnavailableException extends ApiException {
        private ServiceUnavailableException() {
            super(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
