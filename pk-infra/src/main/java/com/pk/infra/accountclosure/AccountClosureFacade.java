package com.pk.infra.accountclosure;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.LenderUserDisablePort;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;

/**
 * Customer-service submit path only. App pre-check stays on {@code AuthApplicationService} +
 * {@code AccountCloseAccessFacade} with real {@code user_device}; this facade calls lender
 * {@code /user/disable} then enqueues locally (no duplicate local gate before disable).
 */
public class AccountClosureFacade {
    private final AccountClosureMapper accountClosureMapper;
    private final LenderUserDisablePort lenderUserDisablePort;
    private final AccountClosureLocalWriter localWriter;

    public AccountClosureFacade(
            AccountClosureMapper accountClosureMapper,
            LenderUserDisablePort lenderUserDisablePort,
            AccountClosureLocalWriter localWriter
    ) {
        this.accountClosureMapper = accountClosureMapper;
        this.lenderUserDisablePort = lenderUserDisablePort;
        this.localWriter = localWriter;
    }

    /**
     * CS backoffice submit: {@code /user/disable} first (lender gate), then enqueue locally in one transaction.
     */
    public AccountClosureSubmitResult submitClosureForUser(
            UserProfileSummary user,
            String reason,
            String operatorId
    ) {
        if (accountClosureMapper.countActiveDeletionQueue(user.userId()) > 0) {
            throw new ApiException(ApiCode.ACCOUNT_CLOSURE_ALREADY_REQUESTED);
        }

        lenderUserDisablePort.disableUser(user.partnerUserId());
        return localWriter.persistClosureLocally(user, operatorId, reason);
    }

    public record AccountClosureSubmitResult(
            long userId,
            String partnerUserId,
            String mobileNo,
            String queueStatus
    ) {
    }
}
