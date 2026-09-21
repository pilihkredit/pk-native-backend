package com.pk.app.ops.support;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.app.ops.config.OpsBackofficeProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class OpsBackofficeAuthSupport {
    private final OpsBackofficeProperties properties;

    public OpsBackofficeAuthSupport(OpsBackofficeProperties properties) {
        this.properties = properties;
    }

    public void requireAuthorized(HttpServletRequest request, String opsTokenHeader) {
        if (!properties.enabled() || !properties.tokenConfigured()) {
            throw new ApiException(ApiCode.OPS_API_DISABLED);
        }
        String token = resolveToken(opsTokenHeader, request);
        if (token == null || !properties.token().equals(token)) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (properties.ipWhitelistConfigured() && !clientIpAllowed(request)) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "Client IP not allowed");
        }
    }

    private static String resolveToken(String opsTokenHeader, HttpServletRequest request) {
        if (opsTokenHeader != null && !opsTokenHeader.isBlank()) {
            return opsTokenHeader.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String bearer = authorization.substring("Bearer ".length()).trim();
            return bearer.isEmpty() ? null : bearer;
        }
        return null;
    }

    private boolean clientIpAllowed(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        return properties.allowedClientIps().stream()
                .filter(ip -> ip != null && !ip.isBlank())
                .map(String::trim)
                .anyMatch(clientIp::equals);
    }

    static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma >= 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
