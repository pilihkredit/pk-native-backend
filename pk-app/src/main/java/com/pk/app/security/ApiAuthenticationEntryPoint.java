package com.pk.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.common.web.ActiveLenderProvider;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.api.ApiCode;
import com.pk.infra.auth.AuthRejectReasons;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;
    private final ActiveLenderProvider activeLenderProvider;
    private final AuthUnauthorizedStructuredLogger authUnauthorizedStructuredLogger;

    public ApiAuthenticationEntryPoint(
            ObjectMapper objectMapper,
            ActiveLenderProvider activeLenderProvider,
            AuthUnauthorizedStructuredLogger authUnauthorizedStructuredLogger
    ) {
        this.objectMapper = objectMapper;
        this.activeLenderProvider = activeLenderProvider;
        this.authUnauthorizedStructuredLogger = authUnauthorizedStructuredLogger;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (JwtAuthenticationFilter.resolveBearerToken(request) == null) {
            authUnauthorizedStructuredLogger.log(request, AuthRejectReasons.TOKEN_NOT_PROVIDED);
            log.warn(
                    "Access token not provided path={} method={}",
                    request.getRequestURI(),
                    request.getMethod()
            );
        } else {
            authUnauthorizedStructuredLogger.log(request, AuthRejectReasons.TOKEN_REJECTED);
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = activeLenderProvider.enrich(
                ApiResponse.failure(ApiCode.UNAUTHORIZED_REQUEST, RequestTrace.resolveTraceId(request))
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
