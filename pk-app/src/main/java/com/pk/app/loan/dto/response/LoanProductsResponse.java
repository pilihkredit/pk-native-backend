package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanProductFacade;
import java.util.List;

public record LoanProductsResponse(
        String applyId,
        String creditStatus,
        String productStatus,
        List<LoanProductResponse> products
) {
    public static LoanProductsResponse from(LoanProductFacade.ProductsResult result) {
        return new LoanProductsResponse(
                result.applyId(),
                result.creditStatus(),
                result.productStatus(),
                result.products().stream().map(LoanProductResponse::from).toList()
        );
    }
}
