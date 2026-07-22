package com.pk.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.common.web.ActiveLenderProvider;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.api.ApiCode;
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

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper, ActiveLenderProvider activeLenderProvider) {
        this.objectMapper = objectMapper;
        this.activeLenderProvider = activeLenderProvider;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (JwtAuthenticationFilter.resolveBearerToken(request) == null) {
            log.warn(
                    "Access token not provided path={} method={}",
                    request.getRequestURI(),
                    request.getMethod()
            );
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
