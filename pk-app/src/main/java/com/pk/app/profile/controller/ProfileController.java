package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.ProfileEnumApplicationService;
import com.pk.app.profile.application.ProfileApplicationService;
import com.pk.app.profile.dto.request.ProfileContactsSaveRequest;
import com.pk.app.profile.dto.request.ProfilePersonalSaveRequest;
import com.pk.app.profile.dto.response.ProfileContactsSaveResponse;
import com.pk.app.profile.dto.response.ProfileEnumsResponse;
import com.pk.app.profile.dto.response.ProfilePersonalSaveResponse;
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

    /**
     * List profile enum options for onboarding forms.
     *
     * Returns canonical integer values for request bodies plus Indonesian display labels.
     * {@code lenderField} documents the Pendanaan OpenAPI field name for future sync mapping.
     *
     * @param module optional filter: personal, work, or contact
     * @param httpRequest servlet request for trace id
     * @return enum field metadata and options
     */
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

    /**
     * Save personal basic information
     *
     * Persists residential address, education, and mother surname during onboarding.
     * Mother surname is encrypted at rest. Idempotent by requestId.
     *
     * @param request     personal module payload
     * @param httpRequest servlet request for trace id
     * @return requestId and moduleStatus COMPLETED
     */
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
                profileApplicationService.savePersonal(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /**
     * Save emergency contacts during onboarding.
     *
     * Requires at least two contacts. Contact mobiles must differ from the user's own number
     * and from each other. Idempotent by requestId.
     *
     * @param request     contacts module payload
     * @param httpRequest servlet request for trace id
     * @return requestId and moduleStatus COMPLETED
     */
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
                profileApplicationService.saveContacts(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
