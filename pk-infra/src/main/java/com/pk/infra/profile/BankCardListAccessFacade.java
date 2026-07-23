package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.List;
import java.util.Set;

/**
 * Real-time lender gate for whether the client may open the bank-card list UI.
 */
public class BankCardListAccessFacade {
    private static final Set<Integer> BLOCKED_LIFE_TIME_STATUSES = Set.of(
            3, // 预审核处理中
            5, // 提现处理中
            6, // 待打款
            7  // 打款中
    );

    private final ProfileQueryFacade profileQueryFacade;
    private final LenderUserStatusPort lenderUserStatusPort;

    public BankCardListAccessFacade(
            ProfileQueryFacade profileQueryFacade,
            LenderUserStatusPort lenderUserStatusPort
    ) {
        this.profileQueryFacade = profileQueryFacade;
        this.lenderUserStatusPort = lenderUserStatusPort;
    }

    public BankCardListAccessResult checkAccess(String partnerUserId, LenderDeviceContext device) {
        if (partnerUserId == null || partnerUserId.isBlank() || device == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!hasLenderBankCard(partnerUserId)) {
            throw new ApiException(ApiCode.BANK_CARD_LIST_ACCESS_DENIED);
        }
        Integer status = lenderUserStatusPort.queryStatus(
                new LenderUserStatusPort.LenderUserStatusCommand(partnerUserId, device)
        ).userLoanLifeTimeStatus();
        if (status != null && BLOCKED_LIFE_TIME_STATUSES.contains(status)) {
            throw new ApiException(ApiCode.BANK_CARD_LIST_ACCESS_DENIED);
        }
        return new BankCardListAccessResult(true);
    }

    private boolean hasLenderBankCard(String partnerUserId) {
        JsonNode root = profileQueryFacade.query(partnerUserId, List.of("bankCard"));
        JsonNode list = root.path("bankCardList");
        return list.isArray() && !list.isEmpty();
    }

    public record BankCardListAccessResult(boolean canShowList) {
    }
}
