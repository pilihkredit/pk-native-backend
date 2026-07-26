package com.pk.core.auth;

public record AuthenticatedPrincipal(
        long userId,
        String partnerUserId,
        String mobileNo,
        long sessionVersion
) {
}
