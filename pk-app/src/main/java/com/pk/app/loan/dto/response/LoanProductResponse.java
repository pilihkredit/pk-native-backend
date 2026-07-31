package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanProductFacade;
import java.math.BigDecimal;
import java.util.List;

public record LoanProductResponse(
        String productCode,
        String productName,
        BigDecimal minAmount,
        String minAmountDisplay,
        BigDecimal maxAmount,
        String maxAmountDisplay,
        BigDecimal comprehensiveRate,
        String comprehensiveRateDisplay,
        List<RepayMethodResponse> repayMethods
) {
    public static LoanProductResponse from(LoanProductFacade.ProductResult result) {
        return new LoanProductResponse(
                result.productCode(),
                result.productName(),
                result.minAmount(),
                result.minAmountDisplay(),
                result.maxAmount(),
                result.maxAmountDisplay(),
                result.comprehensiveRate(),
                result.comprehensiveRateDisplay(),
                result.repayMethods().stream().map(RepayMethodResponse::from).toList()
        );
    }
}
