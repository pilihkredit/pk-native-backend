package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.RepaymentUniformity;
import com.pk.infra.credit.CreditExternalStatusMapper;
import java.math.BigDecimal;
import java.util.List;

public class LoanProductFacade {
    static final String FRONTEND_PRODUCT_NAME = "KTA Kilat Plus";

    private final CreditApplicationRepository creditApplicationRepository;
    private final ProductListResolver productListResolver;

    public LoanProductFacade(
            CreditApplicationRepository creditApplicationRepository,
            ProductListResolver productListResolver
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.productListResolver = productListResolver;
    }

    public ProductsResult listProducts(long userId, String applyId) {
        return listProducts(userId, applyId, false);
    }

    public ProductsResult listProductsForceRefresh(long userId, String applyId) {
        return listProducts(userId, applyId, true);
    }

    public ProductsResult listProducts(long userId, String applyId, boolean forceRefresh) {
        CreditApplicationRepository.CreditApplicationRecord record = requireApprovedCredit(userId, applyId);
        ProductListResolver.ResolvedProductList resolved =
                productListResolver.resolve(userId, record, forceRefresh);
        return toProductsResult(record.applyId(), resolved);
    }

    public LenderRepayMethod findRepayMethod(long userId, String applyId, String repayMethodCode) {
        if (repayMethodCode == null || repayMethodCode.isBlank()) {
            return null;
        }
        CreditApplicationRepository.CreditApplicationRecord record = requireApprovedCredit(userId, applyId);
        ProductListResolver.ResolvedProductList resolved =
                productListResolver.resolve(userId, record, false);
        for (LenderLoanProduct product : resolved.products()) {
            for (LenderRepayMethod repayMethod : product.repayMethods()) {
                if (repayMethodCode.equals(repayMethod.repayMethod())) {
                    return repayMethod;
                }
            }
        }
        return null;
    }

    public long latestProductListId(long userId, String applyId, boolean forceRefresh) {
        CreditApplicationRepository.CreditApplicationRecord record = requireApprovedCredit(userId, applyId);
        return productListResolver.resolve(userId, record, forceRefresh).productListId();
    }

    public ProductListResolver.ResolvedProductList resolveProductList(
            long userId,
            String applyId,
            boolean forceRefresh
    ) {
        CreditApplicationRepository.CreditApplicationRecord record = requireApprovedCredit(userId, applyId);
        return productListResolver.resolve(userId, record, forceRefresh);
    }

    public void requireProductRepayMethod(
            ProductListResolver.ResolvedProductList resolved,
            String productCode,
            String repayMethod
    ) {
        if (productCode == null || productCode.isBlank() || repayMethod == null || repayMethod.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        for (LenderLoanProduct product : resolved.products()) {
            if (!productCode.equals(product.productCode())) {
                continue;
            }
            for (LenderRepayMethod method : product.repayMethods()) {
                if (repayMethod.equals(method.repayMethod())) {
                    return;
                }
            }
        }
        throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    private CreditApplicationRepository.CreditApplicationRecord requireApprovedCredit(long userId, String applyId) {
        String resolvedApplyId = resolveApplyId(userId, applyId);
        CreditApplicationRepository.CreditApplicationRecord record = creditApplicationRepository
                .findByApplyIdAndUserId(resolvedApplyId, userId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        return record;
    }

    private String resolveApplyId(long userId, String applyId) {
        if (applyId != null && !applyId.isBlank()) {
            return applyId;
        }
        return creditApplicationRepository.findLatestByUserId(userId)
                .map(CreditApplicationRepository.CreditApplicationRecord::applyId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
    }

    private static ProductsResult toProductsResult(String applyId, ProductListResolver.ResolvedProductList resolved) {
        return new ProductsResult(
                applyId,
                resolved.creditStatus(),
                resolved.productStatus(),
                resolved.products().stream().map(LoanProductFacade::toProduct).toList()
        );
    }

    private static ProductResult toProduct(LenderLoanProduct product) {
        return new ProductResult(
                product.productCode(),
                FRONTEND_PRODUCT_NAME,
                product.minAmount(),
                product.maxAmount(),
                product.comprehensiveRate(),
                DisplayFormatters.formatIdrAmount(product.minAmount()),
                DisplayFormatters.formatIdrAmount(product.maxAmount()),
                DisplayFormatters.formatComprehensiveRate(product.comprehensiveRate()),
                product.repayMethods().stream().map(LoanProductFacade::toRepayMethod).toList()
        );
    }

    private static RepayMethodResult toRepayMethod(LenderRepayMethod repayMethod) {
        boolean uniform = RepaymentUniformity.isUniform(repayMethod);
        return new RepayMethodResult(
                repayMethod.repayMethod(),
                repayMethod.cycleType(),
                repayMethod.cycleInterval(),
                repayMethod.cycleCount(),
                repayMethod.totalCycleInterval(),
                repayMethod.repayMethodType(),
                uniform,
                repayMethod.unevenBillsRepaymentRates().stream()
                        .map(rate -> new UnevenBillRateResult(rate.termNum(), rate.repaymentRate()))
                        .toList()
        );
    }

    public record ProductsResult(
            String applyId,
            String creditStatus,
            String productStatus,
            List<ProductResult> products
    ) {
    }

    public record ProductResult(
            String productCode,
            String productName,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal comprehensiveRate,
            String minAmountDisplay,
            String maxAmountDisplay,
            String comprehensiveRateDisplay,
            List<RepayMethodResult> repayMethods
    ) {
    }

    public record RepayMethodResult(
            String repayMethod,
            String cycleType,
            Integer cycleInterval,
            Integer cycleCount,
            Integer totalCycleInterval,
            Integer repayMethodType,
            boolean repaymentUniform,
            List<UnevenBillRateResult> unevenBillsRepaymentRates
    ) {
    }

    public record UnevenBillRateResult(int termNum, BigDecimal repaymentRate) {
    }
}
