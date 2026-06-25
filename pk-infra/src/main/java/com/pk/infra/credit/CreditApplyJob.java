package com.pk.infra.credit;

import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;

public record CreditApplyJob(
        long creditApplicationId,
        String applyId,
        String partnerUserId,
        BigDecimal lat,
        BigDecimal lng,
        String ip,
        String address,
        LenderDeviceContext device,
        List<CreditRiskAppInfo> appList
) {
}
