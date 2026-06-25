package com.pk.app.callback.application;

import com.pk.app.callback.dto.request.CallbackOAuthTokenRequest;
import com.pk.app.callback.dto.response.CallbackOAuthTokenResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.callback.CallbackProperties;
import com.pk.infra.callback.CallbackTokenIssuer;
import com.pk.infra.credit.CreditCallbackIntakeFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CallbackApplicationService {
    private final CallbackProperties callbackProperties;
    private final CallbackTokenIssuer callbackTokenIssuer;
    private final CreditCallbackIntakeFacade creditCallbackIntakeFacade;

    public CallbackApplicationService(
            CallbackProperties callbackProperties,
            CallbackTokenIssuer callbackTokenIssuer,
            CreditCallbackIntakeFacade creditCallbackIntakeFacade
    ) {
        this.callbackProperties = callbackProperties;
        this.callbackTokenIssuer = callbackTokenIssuer;
        this.creditCallbackIntakeFacade = creditCallbackIntakeFacade;
    }

    public CallbackOAuthTokenResponse issueToken(CallbackOAuthTokenRequest request) {
        ensureEnabled();
        if (!"client_credentials".equals(request.grantType())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!callbackProperties.oauth().credentialsConfigured()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if (!callbackProperties.oauth().clientId().equals(request.clientId())
                || !callbackProperties.oauth().clientSecret().equals(request.clientSecret())) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        CallbackTokenIssuer.CallbackAccessToken token = callbackTokenIssuer.issueToken();
        return new CallbackOAuthTokenResponse(token.accessToken(), token.expiresInSeconds());
    }

    @Transactional
    public void receiveCreditResult(String accessToken, String rawPayloadJson) {
        ensureEnabled();
        callbackTokenIssuer.validateToken(accessToken);
        creditCallbackIntakeFacade.intake(rawPayloadJson);
    }

    private void ensureEnabled() {
        if (!callbackProperties.enabled()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
