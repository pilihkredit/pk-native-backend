package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.ProfileEnumApplicationService;
import com.pk.app.profile.application.ProfileApplicationService;
import com.pk.app.profile.dto.request.ProfileBankCardSaveRequest;
import com.pk.app.profile.dto.request.ProfileContactsSaveRequest;
import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.request.ProfileWorkSaveRequest;
import com.pk.app.profile.dto.response.ProfileBankCardSaveResponse;
import com.pk.app.profile.dto.response.ProfileContactsSaveResponse;
import com.pk.app.profile.dto.response.ProfileEnumsResponse;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
import com.pk.app.profile.dto.response.ProfileWorkSaveResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile onboarding modules.
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileApplicationService profileApplicationService;
    private final ProfileEnumApplicationService profileEnumApplicationService;

    public ProfileController(
            ProfileApplicationService profileApplicationService,
            ProfileEnumApplicationService profileEnumApplicationService
    ) {
        this.profileApplicationService = profileApplicationService;
        this.profileEnumApplicationService = profileEnumApplicationService;
    }

    /** List profile enums. */
    @GetMapping("/enums")
    public ApiResponse<ProfileEnumsResponse> listEnums(
            @RequestParam(value = "module", required = false) String module,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileEnumApplicationService.listEnums(module),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save personal info. */
    @PostMapping("/personal")
    public ApiResponse<ProfilePersonalSaveResponse> savePersonal(
            @Valid @RequestBody ProfilePersonalSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.savePersonal(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save work info. */
    @PostMapping("/work")
    public ApiResponse<ProfileWorkSaveResponse> saveWork(
            @Valid @RequestBody ProfileWorkSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.saveWork(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save emergency contacts. */
    @PostMapping("/contacts")
    public ApiResponse<ProfileContactsSaveResponse> saveContacts(
            @Valid @RequestBody ProfileContactsSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.saveContacts(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save bank card. */
    @PostMapping("/bank-card")
    public ApiResponse<ProfileBankCardSaveResponse> saveBankCard(
            @Valid @RequestBody ProfileBankCardSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.saveBankCard(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
