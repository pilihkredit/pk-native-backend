package com.pk.core.repay.port;

import java.util.List;

public interface LenderRepayCurrentOrderPort {
    void setCurrentOrder(LenderRepayCurrentOrderCommand command);

    record LenderRepayCurrentOrderCommand(
            String partnerUserId,
            List<RepayOrderItem> repayOrders,
            Long couponId
    ) {
    }

    record RepayOrderItem(
            String loanApplyId,
            List<Integer> termNos
    ) {
    }
}
