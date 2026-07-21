package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.fasterxml.jackson.databind.JsonNode;
import com.pk.app.profile.application.ProfileQueryApplicationService;
import com.pk.app.profile.application.ProfileEnumApplicationService;
import com.pk.app.profile.application.ProfileApplicationService;
import com.pk.app.profile.dto.request.ProfileAppsFlyerInstallSaveRequest;
import com.pk.app.profile.dto.request.ProfileBankCardSaveRequest;
import com.pk.app.profile.dto.request.ProfileContactsSaveRequest;
import com.pk.app.profile.dto.request.ProfileInfoQueryRequest;
import com.pk.app.profile.dto.request.ProfileLoginLogSaveRequest;
import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.request.ProfileTongdunDeviceSaveRequest;
import com.pk.app.profile.dto.response.ProfileAppsFlyerInstallSaveResponse;
import com.pk.app.profile.dto.response.ProfileBankCardSaveResponse;
import com.pk.app.profile.dto.response.ProfileContactsSaveResponse;
import com.pk.app.profile.dto.response.ProfileEnumsResponse;
import com.pk.app.profile.dto.response.ProfileLoginLogSaveResponse;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
import com.pk.app.profile.dto.response.ProfileTongdunDeviceSaveResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.PublicApi;
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
    private final ProfileQueryApplicationService profileQueryApplicationService;
    private final ProfileEnumApplicationService profileEnumApplicationService;

    public ProfileController(
            ProfileApplicationService profileApplicationService,
            ProfileQueryApplicationService profileQueryApplicationService,
            ProfileEnumApplicationService profileEnumApplicationService
    ) {
        this.profileApplicationService = profileApplicationService;
        this.profileQueryApplicationService = profileQueryApplicationService;
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

    /** Query synced profile modules from lender. */
    @PostMapping("/info/query")
    public ApiResponse<JsonNode> queryInfo(
            @Valid @RequestBody ProfileInfoQueryRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileQueryApplicationService.query(principal, request),
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

    /** Save login log and sync to lender. */
    @PostMapping("/login-log")
    public ApiResponse<ProfileLoginLogSaveResponse> saveLoginLog(
            @Valid @RequestBody ProfileLoginLogSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.saveLoginLog(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save AppsFlyer install data locally (public). Lender sync attaches AF on identity upsert when logged in. */
    @PublicApi
    @PostMapping("/appsflyer-install")
    public ApiResponse<ProfileAppsFlyerInstallSaveResponse> saveAppsFlyerInstall(
            @Valid @RequestBody ProfileAppsFlyerInstallSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                profileApplicationService.saveAppsFlyerInstall(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Save Tongdun device fingerprint and sync to lender. */
    @PostMapping("/tongdun-device")
    public ApiResponse<ProfileTongdunDeviceSaveResponse> saveTongdunDevice(
            @Valid @RequestBody ProfileTongdunDeviceSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                profileApplicationService.saveTongdunDevice(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
