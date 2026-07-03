package com.pk.core.loan.port;

import com.pk.core.loan.LenderLoanProduct;
import java.util.List;

public interface LenderLoanProductPort {
    LenderLoanProductListResult listProducts(String applyId);

    record LenderLoanProductListResult(
            String applyId,
            String creditApplyNo,
            String userId,
            String externalCreditStatus,
            String productStatus,
            List<LenderLoanProduct> products
    ) {
    }
}
