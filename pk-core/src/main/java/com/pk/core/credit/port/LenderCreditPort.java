package com.pk.core.credit.port;

import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;

public interface LenderCreditPort {
    LenderCreditApplyResult apply(LenderCreditApplyCommand command);

    LenderCreditStatusResult queryStatus(String applyId);

    record LenderCreditApplyCommand(
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

    record LenderCreditApplyResult(String creditApplyNo, String externalUserId) {
    }

    record LenderCreditStatusResult(
            String externalStatus,
            String creditApplyNo,
            Long creditContractExpireTime,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize
    ) {
    }
}
