package com.pk.infra.accountclosure;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;
import com.pk.infra.auth.UserSessionInvalidator;
import com.pk.infra.auth.mapper.UserAuthMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atomic local enqueue + profile close after lender disable. */
@Service
public class AccountClosureLocalWriter {
    private static final int REASON_MAX_LEN = 200;

    private final UserAuthMapper userAuthMapper;
    private final AccountClosureMapper accountClosureMapper;
    private final UserSessionInvalidator userSessionInvalidator;

    public AccountClosureLocalWriter(
            UserAuthMapper userAuthMapper,
            AccountClosureMapper accountClosureMapper,
            UserSessionInvalidator userSessionInvalidator
    ) {
        this.userAuthMapper = userAuthMapper;
        this.accountClosureMapper = accountClosureMapper;
        this.userSessionInvalidator = userSessionInvalidator;
    }

    @Transactional
    public AccountClosureFacade.AccountClosureSubmitResult persistClosureLocally(
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
        userSessionInvalidator.invalidateAll(user.userId());
        return new AccountClosureFacade.AccountClosureSubmitResult(
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
}
