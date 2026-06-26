package com.pk.app.callback.controller;

import com.pk.app.callback.application.CallbackApplicationService;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback/loan")
@PublicApi
public class CallbackLoanController {
    private final CallbackApplicationService callbackApplicationService;

    public CallbackLoanController(CallbackApplicationService callbackApplicationService) {
        this.callbackApplicationService = callbackApplicationService;
    }

    @PostMapping("/result")
    public ApiResponse<Void> receiveResult(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody String rawPayloadJson,
            HttpServletRequest httpRequest
    ) {
        String accessToken = resolveBearerToken(authorization);
        if (accessToken == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        callbackApplicationService.receiveLoanResult(accessToken, rawPayloadJson);
        return ApiResponse.success(null, RequestTrace.resolveTraceId(httpRequest));
    }

    private static String resolveBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }
}
