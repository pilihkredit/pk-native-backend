package com.pk.infra.accountclosure;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.LenderUserDisablePort;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;
import com.pk.infra.auth.mapper.UserAuthMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer-service submit path only. App pre-check stays on {@code AuthApplicationService} +
 * {@code AccountCloseAccessFacade} with real {@code user_device}; this facade calls lender
 * {@code /user/disable} then enqueues locally (no duplicate local gate before disable).
 */
public class AccountClosureFacade {
    private static final int REASON_MAX_LEN = 200;

    private final UserAuthMapper userAuthMapper;
    private final AccountClosureMapper accountClosureMapper;
    private final LenderUserDisablePort lenderUserDisablePort;

    public AccountClosureFacade(
            UserAuthMapper userAuthMapper,
            AccountClosureMapper accountClosureMapper,
            LenderUserDisablePort lenderUserDisablePort
    ) {
        this.userAuthMapper = userAuthMapper;
        this.accountClosureMapper = accountClosureMapper;
        this.lenderUserDisablePort = lenderUserDisablePort;
    }

    /**
     * CS backoffice submit: {@code /user/disable} first (lender gate), then enqueue locally.
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
        return persistClosureLocally(user, operatorId, reason);
    }

    @Transactional
    AccountClosureSubmitResult persistClosureLocally(
            UserProfileSummary user,
            String operatorId,
            String reason
    ) {
        String storedReason = formatReason(operatorId, reason);
        int inserted = accountClosureMapper.insertDeletionQueue(
                user.userId(),
                user.partnerUserId(),
                user.mobileNo(),
                storedReason
        );
        if (inserted != 1) {
            throw new ApiException(ApiCode.INTERNAL_SERVER_ERROR, "Failed to enqueue account closure");
        }
        int closed = userAuthMapper.markAccountClosed(user.userId());
        if (closed != 1) {
            throw new ApiException(ApiCode.INTERNAL_SERVER_ERROR, "Failed to mark user profile closed");
        }
        return new AccountClosureSubmitResult(
                user.userId(),
                user.partnerUserId(),
                user.mobileNo(),
                "pending"
        );
    }

    private static String formatReason(String operatorId, String reason) {
        String trimmedReason = reason == null ? "" : reason.trim();
        String trimmedOperator = operatorId == null ? "" : operatorId.trim();
        if (trimmedOperator.isEmpty()) {
            return truncate(trimmedReason, REASON_MAX_LEN);
        }
        if (trimmedReason.isEmpty()) {
            return truncate("operator=" + trimmedOperator, REASON_MAX_LEN);
        }
        return truncate("operator=" + trimmedOperator + "; " + trimmedReason, REASON_MAX_LEN);
    }

    private static String truncate(String value, int maxLen) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    public record AccountClosureSubmitResult(
            long userId,
            String partnerUserId,
            String mobileNo,
            String queueStatus
    ) {
    }
}
