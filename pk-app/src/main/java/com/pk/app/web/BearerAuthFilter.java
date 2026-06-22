package com.pk.app.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.api.ApiResponse;
import com.pk.app.auth.JwtTokenService;
import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import com.pk.core.error.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerAuthFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/pk/v1/platform/status",
            "/api/pk/v1/app/disclosure/permission",
            "/api/pk/v1/auth/mobile/check",
            "/api/pk/v1/auth/otp/send",
            "/api/pk/v1/auth/otp/verify"
    );

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public BearerAuthFilter(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (pathMatcher.match("/actuator/**", path)) {
            return true;
        }
        return PUBLIC_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                writeError(response, request, AppErrorCodes.UNAUTHORIZED);
                return;
            }
            String token = authorization.substring("Bearer ".length()).trim();
            if (token.isEmpty()) {
                writeError(response, request, AppErrorCodes.UNAUTHORIZED);
                return;
            }
            AuthenticatedUser user = jwtTokenService.parseToken(token);
            AuthContext.set(user);
            filterChain.doFilter(request, response);
        } catch (AppBusinessException ex) {
            writeError(response, request, ex.code());
        } finally {
            AuthContext.clear();
        }
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, String code)
            throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = new ApiResponse<>(
                code,
                ErrorMessages.forCode(code),
                null,
                RequestSupport.traceId(request)
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
