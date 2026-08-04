package com.pk.app.push.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.push.application.PushApplicationService;
import com.pk.app.push.dto.request.PushDeviceRegisterRequest;
import com.pk.app.push.dto.response.PushDeviceRegisterResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/push/devices")
public class PushDeviceController {
    private final PushApplicationService pushApplicationService;

    public PushDeviceController(PushApplicationService pushApplicationService) {
        this.pushApplicationService = pushApplicationService;
    }

    @PublicApi
    @PostMapping("/register")
    public ApiResponse<PushDeviceRegisterResponse> register(
            @Valid @RequestBody PushDeviceRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                pushApplicationService.registerDevice(
                        SecurityContextSupport.requirePrincipal(),
                        request,
                        ClientRequestHeaders.require(httpRequest)
                ),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
