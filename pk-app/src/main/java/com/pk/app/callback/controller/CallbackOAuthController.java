package com.pk.app.callback.controller;

import com.pk.app.callback.application.CallbackApplicationService;
import com.pk.app.callback.dto.request.CallbackOAuthTokenRequest;
import com.pk.app.callback.dto.response.CallbackOAuthTokenResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@PublicApi
public class CallbackOAuthController {
    private final CallbackApplicationService callbackApplicationService;

    public CallbackOAuthController(CallbackApplicationService callbackApplicationService) {
        this.callbackApplicationService = callbackApplicationService;
    }

    @PostMapping("/token")
    public ApiResponse<CallbackOAuthTokenResponse> token(
            @Valid @RequestBody CallbackOAuthTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                callbackApplicationService.issueToken(request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
