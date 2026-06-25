package com.pk.app.area.controller;

import com.pk.app.area.application.AreaApplicationService;
import com.pk.app.area.dto.request.AreaListRequest;
import com.pk.app.area.dto.response.AreaListItemResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative area reference data for onboarding.
 */
@RestController
@RequestMapping("/area")
public class AreaController {
    private final AreaApplicationService areaApplicationService;

    public AreaController(AreaApplicationService areaApplicationService) {
        this.areaApplicationService = areaApplicationService;
    }

    /** List areas. */
    @PostMapping("/list")
    public ApiResponse<List<AreaListItemResponse>> listAreas(
            @Valid @RequestBody(required = false) AreaListRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        String parentCode = request == null ? null : request.parentCode();
        return ApiResponse.success(
                areaApplicationService.listAreas(parentCode),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
