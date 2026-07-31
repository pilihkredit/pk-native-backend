package com.pk.infra.loan;

import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;

public record LoanApplyJob(
        long loanApplicationId,
        String loanApplyId,
        String creditApplyId,
        String mobileNo,
        BigDecimal applyAmt,
        String productCode,
        String repayMethod,
        String loanPurpose,
        Long couponId,
        BigDecimal lat,
        BigDecimal lng,
        String ip,
        String address,
        String adId,
        LenderDeviceContext device,
        List<CreditRiskAppInfo> appList
) {
}
