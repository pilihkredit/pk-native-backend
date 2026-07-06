package com.pk.app.security;

import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.external.LenderInteractionContext;
import com.pk.infra.auth.AuthServiceFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthServiceFacade authServiceFacade;
    private final PublicApiEndpointRegistry publicApiEndpointRegistry;
    private final ApiExceptionResponseWriter apiExceptionResponseWriter;

    public JwtAuthenticationFilter(
            AuthServiceFacade authServiceFacade,
            PublicApiEndpointRegistry publicApiEndpointRegistry,
            ApiExceptionResponseWriter apiExceptionResponseWriter
    ) {
        this.authServiceFacade = authServiceFacade;
        this.publicApiEndpointRegistry = publicApiEndpointRegistry;
        this.apiExceptionResponseWriter = apiExceptionResponseWriter;
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
                    SecurityContextHolder.getContext().setAuthentication(authentication(principal));
                    LenderInteractionContext.setMobileNo(principal.mobileNo());
                } catch (ApiException exception) {
                    if (!publicApiEndpointRegistry.isPublic(request)) {
                        apiExceptionResponseWriter.write(request, response, exception);
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            LenderInteractionContext.clear();
        }
    }

    private static UsernamePasswordAuthenticationToken authentication(AuthenticatedPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    static String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }
}
