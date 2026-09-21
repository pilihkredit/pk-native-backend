package com.pk.app.ops.application;

import com.pk.app.ops.dto.OpsAccountClosureEligibilityRequest;
import com.pk.app.ops.dto.OpsAccountClosureEligibilityResponse;
import com.pk.app.ops.dto.OpsAccountClosureSubmitRequest;
import com.pk.app.ops.dto.OpsAccountClosureSubmitResponse;
import com.pk.app.ops.support.OpsAccountClosureUserResolver;
import com.pk.app.ops.support.OpsBackofficeAuthSupport;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.AccountClosureStatusDeviceProvider;
import com.pk.infra.accountclosure.AccountClosureFacade;
import com.pk.infra.auth.AccountCloseAccessFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * Customer-service account closure (Ops). Separate from App {@code POST /auth/close-account/check}.
 */
@Service
public class OpsAccountClosureApplicationService {
    private final OpsBackofficeAuthSupport opsBackofficeAuthSupport;
    private final AccountClosureFacade accountClosureFacade;
    private final OpsAccountClosureUserResolver userResolver;
    private final AccountCloseAccessFacade accountCloseAccessFacade;
    private final AccountClosureStatusDeviceProvider opsStatusDeviceProvider;

    public OpsAccountClosureApplicationService(
            OpsBackofficeAuthSupport opsBackofficeAuthSupport,
            AccountClosureFacade accountClosureFacade,
            OpsAccountClosureUserResolver userResolver,
            AccountCloseAccessFacade accountCloseAccessFacade,
            AccountClosureStatusDeviceProvider opsStatusDeviceProvider
    ) {
        this.opsBackofficeAuthSupport = opsBackofficeAuthSupport;
        this.accountClosureFacade = accountClosureFacade;
        this.userResolver = userResolver;
        this.accountCloseAccessFacade = accountCloseAccessFacade;
        this.opsStatusDeviceProvider = opsStatusDeviceProvider;
    }

    public OpsAccountClosureSubmitResponse submit(
            HttpServletRequest httpRequest,
            String opsToken,
            OpsAccountClosureSubmitRequest request
    ) {
        opsBackofficeAuthSupport.requireAuthorized(httpRequest, opsToken);
        UserProfileSummary user = userResolver.resolveActiveUser(
                request.userId(),
                request.mobileNo(),
                request.partnerUserId()
        );
        var result = accountClosureFacade.submitClosureForUser(
                user,
                request.reason(),
                request.operatorId()
        );
        return new OpsAccountClosureSubmitResponse(
                result.userId(),
                result.partnerUserId(),
                result.mobileNo(),
                result.queueStatus()
        );
    }

    /** Optional CS preview only; does not gate {@link #submit}. Uses ops stub device, not App {@code user_device}. */
    public OpsAccountClosureEligibilityResponse checkEligibility(
            HttpServletRequest httpRequest,
            String opsToken,
            OpsAccountClosureEligibilityRequest request
    ) {
        opsBackofficeAuthSupport.requireAuthorized(httpRequest, opsToken);
        UserProfileSummary user = userResolver.resolveActiveUser(
                request.userId(),
                request.mobileNo(),
                request.partnerUserId()
        );
        AuthenticatedPrincipal principal = toPrincipal(user);
        AccountCloseAccessFacade.AccountCloseAccessResult eligibility = accountCloseAccessFacade.checkAccess(
                principal,
                opsStatusDeviceProvider.create()
        );
        return new OpsAccountClosureEligibilityResponse(
                user.userId(),
                user.partnerUserId(),
                user.mobileNo(),
                eligibility.canClose(),
                eligibility.prompt(),
                eligibility.reason()
        );
    }

    private static AuthenticatedPrincipal toPrincipal(UserProfileSummary user) {
        return new AuthenticatedPrincipal(user.userId(), user.partnerUserId(), user.mobileNo(), 0L);
    }
}
