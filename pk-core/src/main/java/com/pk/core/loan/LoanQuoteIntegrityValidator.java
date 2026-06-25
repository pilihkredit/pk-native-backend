package com.pk.core.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.port.LoanQuoteRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

public final class LoanQuoteIntegrityValidator {
    private LoanQuoteIntegrityValidator() {
    }

    public static void validateNotExpired(Instant quotedAt, Duration ttl) {
        if (quotedAt == null) {
            throw new ApiException(ApiCode.QUOTE_SNAPSHOT_MISMATCH);
        }
        if (quotedAt.plus(ttl).isBefore(Instant.now())) {
            throw new ApiException(ApiCode.QUOTE_SNAPSHOT_EXPIRED);
        }
    }

    public static void validateIntegrity(LoanQuoteRepository.LoanQuoteRecord quote, int termCount) {
        if (quote.productCode() == null
                || quote.productCode().isBlank()
                || quote.repayMethod() == null
                || quote.repayMethod().isBlank()
                || quote.applyAmt() == null
                || quote.loanPrincipal() == null
                || quote.payAmount() == null
                || quote.schdAmount() == null
                || quote.interest() == null
                || termCount <= 0) {
            throw new ApiException(ApiCode.QUOTE_SNAPSHOT_MISMATCH);
        }
    }

    public static String computeHash(
            String productCode,
            String repayMethod,
            BigDecimal applyAmt,
            BigDecimal loanPrincipal,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            int termCount
    ) {
        String canonical = String.join(
                "|",
                productCode,
                repayMethod,
                toPlain(applyAmt),
                toPlain(loanPrincipal),
                toPlain(payAmount),
                toPlain(schdAmount),
                toPlain(interest),
                Integer.toString(termCount)
        );
        return sha256Hex(canonical);
    }

    public static String computeHash(LoanQuoteRepository.LoanQuoteRecord quote, int termCount) {
        return computeHash(
                quote.productCode(),
                quote.repayMethod(),
                quote.applyAmt(),
                quote.loanPrincipal(),
                quote.payAmount(),
                quote.schdAmount(),
                quote.interest(),
                termCount
        );
    }

    private static String toPlain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
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
