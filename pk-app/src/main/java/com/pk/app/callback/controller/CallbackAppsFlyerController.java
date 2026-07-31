package com.pk.app.callback.controller;

import com.pk.app.callback.application.CallbackApplicationService;
import com.pk.app.callback.dto.response.CallbackAppsFlyerResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.auth.PublicApi;
import com.pk.infra.callback.AppsFlyerCallbackIntakeFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback/appsflyer")
@PublicApi
public class CallbackAppsFlyerController {
    private final CallbackApplicationService callbackApplicationService;

    public CallbackAppsFlyerController(CallbackApplicationService callbackApplicationService) {
        this.callbackApplicationService = callbackApplicationService;
    }

    @PostMapping
    public ApiResponse<CallbackAppsFlyerResponse> receive(
            @RequestBody String rawPayloadJson,
            HttpServletRequest httpRequest
    ) {
        AppsFlyerCallbackIntakeFacade.IntakeResult result =
                callbackApplicationService.receiveAppsFlyerCallback(rawPayloadJson);
        return ApiResponse.success(
                new CallbackAppsFlyerResponse(result.id(), result.status()),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
