package com.pk.app.disclosure.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.disclosure.application.DisclosureApplicationService;
import com.pk.app.disclosure.dto.response.DisclosurePermissionResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/disclosure")
public class DisclosureController {
    private final DisclosureApplicationService disclosureApplicationService;

    public DisclosureController(DisclosureApplicationService disclosureApplicationService) {
        this.disclosureApplicationService = disclosureApplicationService;
    }

    @PublicApi
    @GetMapping("/permission")
    public ApiResponse<DisclosurePermissionResponse> getPermission(
            @RequestParam(value = "scene", required = false) String scene,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                disclosureApplicationService.getPermission(scene, acceptLanguage),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
