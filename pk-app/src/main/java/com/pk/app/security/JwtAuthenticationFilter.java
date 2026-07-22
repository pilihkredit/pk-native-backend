package com.pk.app.security;

import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.logging.LogContext;
import com.pk.infra.auth.AuthRejectReasons;
import com.pk.infra.auth.AuthServiceFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AuthServiceFacade authServiceFacade;
    private final PublicApiEndpointRegistry publicApiEndpointRegistry;
    private final ApiExceptionResponseWriter apiExceptionResponseWriter;
    private final AuthUnauthorizedStructuredLogger authUnauthorizedStructuredLogger;

    public JwtAuthenticationFilter(
            AuthServiceFacade authServiceFacade,
            PublicApiEndpointRegistry publicApiEndpointRegistry,
            ApiExceptionResponseWriter apiExceptionResponseWriter,
            AuthUnauthorizedStructuredLogger authUnauthorizedStructuredLogger
    ) {
        this.authServiceFacade = authServiceFacade;
        this.publicApiEndpointRegistry = publicApiEndpointRegistry;
        this.apiExceptionResponseWriter = apiExceptionResponseWriter;
        this.authUnauthorizedStructuredLogger = authUnauthorizedStructuredLogger;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String bearerToken = resolveBearerToken(request);
            if (bearerToken != null) {
                try {
                    AuthenticatedPrincipal principal = authServiceFacade.validateAccessToken(bearerToken);
                    UsernamePasswordAuthenticationToken authentication = authentication(principal);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    LenderInteractionContext.setMobileNo(principal.mobileNo());
                    LogContext.putMobileNo(principal.mobileNo());
                } catch (ApiException exception) {
                    if (!publicApiEndpointRegistry.isPublic(request)) {
                        String reason = AuthRejectReasons.consume();
                        if (reason == null) {
                            reason = AuthRejectReasons.TOKEN_REJECTED;
                        }
                        authUnauthorizedStructuredLogger.log(request, reason);
                        log.warn(
                                "Access token rejected path={} method={} code={} reason={}",
                                request.getRequestURI(),
                                request.getMethod(),
                                exception.apiCode() == null ? null : exception.apiCode().code(),
                                reason
                        );
                        apiExceptionResponseWriter.write(request, response, exception);
                        return;
                    }
                    AuthRejectReasons.clear();
                    // Public APIs (e.g. tracking) continue anonymously; log why login identity is dropped.
                    log.warn(
                            "public API ignored invalid access token path={} method={} code={} msg={}",
                            request.getRequestURI(),
                            request.getMethod(),
                            exception.apiCode() == null ? null : exception.apiCode().code(),
                            exception.getMessage()
                    );
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            LenderInteractionContext.clear();
            AuthRejectReasons.clear();
        }
    }

    private static UsernamePasswordAuthenticationToken authentication(AuthenticatedPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public static String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }
}
