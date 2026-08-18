package com.pk.adapter.apipartner;

import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderLoanProductPort;
import java.math.BigDecimal;
import java.util.List;

public class FakeApiPartnerLoanProductAdapter implements LenderLoanProductPort {
    @Override
    public LenderLoanProductListResult listProducts(String applyId) {
        return new LenderLoanProductListResult(
                applyId,
                "CA-FAKE-" + applyId,
                "USR-FAKE",
                "SUCCESS",
                "READY",
                List.of(
                        new LenderLoanProduct(
                                "PD001",
                                "Cash Loan",
                                new BigDecimal("500000"),
                                new BigDecimal("3000000"),
                                "M",
                                new BigDecimal("0.18"),
                                List.of(
                                        new LenderRepayMethod(
                                                "RP001",
                                                "D",
                                                30,
                                                6,
                                                180,
                                                0,
                                                null,
                                                List.of()
                                        ),
                                        new LenderRepayMethod(
                                                "RP002",
                                                "D",
                                                30,
                                                2,
                                                60,
                                                1,
                                                "[{\"termNum\":1,\"repaymentRate\":0.6},{\"termNum\":2,\"repaymentRate\":0.4}]",
                                                List.of(
                                                        new LenderRepayMethod.UnevenBillRate(
                                                                1,
                                                                new BigDecimal("0.6")
                                                        ),
                                                        new LenderRepayMethod.UnevenBillRate(
                                                                2,
                                                                new BigDecimal("0.4")
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                null
        );
    }
}
