package com.pk.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PublicApiEndpointRegistryTest {
    @Test
    void matchesExactAndWildcardPatterns() {
        PublicApiEndpointRegistry.PublicEndpoint exact = new PublicApiEndpointRegistry.PublicEndpoint("POST", "/api/v1/auth/otp/send");
        PublicApiEndpointRegistry.PublicEndpoint wildcard = new PublicApiEndpointRegistry.PublicEndpoint(null, "/api/v1/auth/**");

        assertThat(exact.matches("POST", "/api/v1/auth/otp/send")).isTrue();
        assertThat(exact.matches("GET", "/api/v1/auth/otp/send")).isFalse();
        assertThat(wildcard.matches("GET", "/api/v1/auth/refresh")).isTrue();
    }
}
