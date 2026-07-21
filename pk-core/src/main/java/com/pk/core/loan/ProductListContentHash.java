package com.pk.core.loan;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Stable SHA-256 over normalized product-list business content (no productName / raw JSON).
 */
public final class ProductListContentHash {
    private ProductListContentHash() {
    }

    public static String sha256(
            String creditStatus,
            String productStatus,
            String creditApplyNo,
            String lenderUserId,
            List<LenderLoanProduct> products
    ) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, creditStatus);
        canonical.append('|');
        append(canonical, productStatus);
        canonical.append('|');
        append(canonical, creditApplyNo);
        canonical.append('|');
        append(canonical, lenderUserId);
        canonical.append('|');
        if (products != null) {
            for (LenderLoanProduct product : products) {
                appendProduct(canonical, product);
                canonical.append(';');
            }
        }
        return sha256Hex(canonical.toString());
    }

    private static void appendProduct(StringBuilder canonical, LenderLoanProduct product) {
        if (product == null) {
            return;
        }
        append(canonical, product.productCode());
        canonical.append('#');
        appendDecimal(canonical, product.minAmount());
        canonical.append('#');
        appendDecimal(canonical, product.maxAmount());
        canonical.append('#');
        append(canonical, product.comprehensiveRateUnit());
        canonical.append('#');
        appendDecimal(canonical, product.comprehensiveRate());
        canonical.append('#');
        List<LenderRepayMethod> methods = product.repayMethods();
        if (methods == null) {
            return;
        }
        for (LenderRepayMethod method : methods) {
            appendMethod(canonical, method);
            canonical.append(',');
        }
    }

    private static void appendMethod(StringBuilder canonical, LenderRepayMethod method) {
        if (method == null) {
            return;
        }
        append(canonical, method.repayMethod());
        canonical.append('~');
        append(canonical, method.cycleType());
        canonical.append('~');
        appendInt(canonical, method.cycleInterval());
        canonical.append('~');
        appendInt(canonical, method.cycleCount());
        canonical.append('~');
        appendInt(canonical, method.totalCycleInterval());
        canonical.append('~');
        appendInt(canonical, method.repayMethodType());
        canonical.append('~');
        List<LenderRepayMethod.UnevenBillRate> rates = method.unevenBillsRepaymentRates();
        if (rates == null) {
            return;
        }
        for (LenderRepayMethod.UnevenBillRate rate : rates) {
            if (rate == null) {
                continue;
            }
            canonical.append(rate.termNum());
            canonical.append('=');
            appendDecimal(canonical, rate.repaymentRate());
            canonical.append('/');
        }
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null) {
            builder.append("");
            return;
        }
        builder.append(value.trim());
    }

    private static void appendInt(StringBuilder builder, Integer value) {
        if (value == null) {
            builder.append("");
            return;
        }
        builder.append(value);
    }

    private static void appendDecimal(StringBuilder builder, BigDecimal value) {
        if (value == null) {
            builder.append("");
            return;
        }
        builder.append(value.stripTrailingZeros().toPlainString());
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
