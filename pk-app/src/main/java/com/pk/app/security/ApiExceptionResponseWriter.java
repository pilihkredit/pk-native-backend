package com.pk.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiExceptionResponseWriter {
    private final ObjectMapper objectMapper;

    public ApiExceptionResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, ApiException exception) throws IOException {
        HttpStatus status = resolveStatus(exception.apiCode());
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(exception.apiCode(), RequestTrace.resolveTraceId(request));
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static HttpStatus resolveStatus(ApiCode apiCode) {
        return switch (apiCode) {
            case UNAUTHORIZED_REQUEST -> HttpStatus.UNAUTHORIZED;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
