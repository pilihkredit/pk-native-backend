package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.Set;

/** Real-time lender gate for account closure. */
public class AccountCloseAccessFacade {
    private static final Set<Integer> BLOCKED_LIFE_TIME_STATUSES = Set.of(3, 5, 6, 7);

    private final LenderUserStatusPort lenderUserStatusPort;

    public AccountCloseAccessFacade(LenderUserStatusPort lenderUserStatusPort) {
        this.lenderUserStatusPort = lenderUserStatusPort;
    }

    public AccountCloseAccessResult checkAccess(
            AuthenticatedPrincipal principal,
            LenderDeviceContext device
    ) {
        if (principal == null || principal.partnerUserId() == null || principal.partnerUserId().isBlank() || device == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        LenderUserStatusPort.LenderUserStatusResult status = lenderUserStatusPort.queryStatus(
                new LenderUserStatusPort.LenderUserStatusCommand(principal.partnerUserId(), device)
        );
        boolean blocked = BLOCKED_LIFE_TIME_STATUSES.contains(status.userLoanLifeTimeStatus())
                || (status.onLoanCount() != null && status.onLoanCount() != 0);
        if (blocked) {
            throw new ApiException(ApiCode.ACCOUNT_CLOSE_NOT_ALLOWED);
        }
        return new AccountCloseAccessResult(true);
    }

    public record AccountCloseAccessResult(boolean canClose) {
    }
}
