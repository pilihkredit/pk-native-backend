package com.pk.core.auth.port;

import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.TokenPair;

public interface TokenIssuer {
    TokenPair issue(long profileId, String partnerUserId, String mobileNo, long sessionVersion, String deviceId);

    AuthenticatedPrincipal parseAccessToken(String accessToken);
}
