package com.pk.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JwtAuthenticationFilterTest {
    @Test
    void resolvesBearerToken() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer token-abc");

        assertThat(JwtAuthenticationFilter.resolveBearerToken(request)).isEqualTo("token-abc");
    }

    @Test
    void ignoresMissingAuthorizationHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        assertThat(JwtAuthenticationFilter.resolveBearerToken(request)).isNull();
    }
}
