package com.pk.core.auth;

public record AuthenticatedPrincipal(
        long profileId,
        String partnerUserId,
        String mobileNo,
        long sessionVersion
) {
}
