package com.pk.adapter.pendanaan;

import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;

public class FakePendanaanCreditAdapter implements LenderCreditPort {
    @Override
    public LenderCreditApplyResult apply(LenderCreditApplyCommand command) {
        return new LenderCreditApplyResult("CA-FAKE-" + command.applyId(), "USR-FAKE", null, null);
    }

    @Override
    public LenderCreditStatusResult queryStatus(String applyId) {
        return new LenderCreditStatusResult(
                "SUCCESS",
                "USR-FAKE",
                "CA-FAKE-" + applyId,
                null,
                null,
                new BigDecimal("500000"),
                new BigDecimal("3000000"),
                new BigDecimal("2500000"),
                new BigDecimal("2800000"),
                new BigDecimal("100000"),
                null,
                null
        );
    }
}
