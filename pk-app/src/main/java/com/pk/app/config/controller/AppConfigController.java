package com.pk.app.config.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.config.application.AppConfigApplicationService;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/config")
public class AppConfigController {
    private final AppConfigApplicationService appConfigApplicationService;

    public AppConfigController(AppConfigApplicationService appConfigApplicationService) {
        this.appConfigApplicationService = appConfigApplicationService;
    }

    @PublicApi
    @GetMapping
    public ApiResponse<JsonNode> getByKey(
            @RequestParam("key") String key,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                appConfigApplicationService.getByKey(key),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
