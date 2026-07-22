package com.pk.core.loan.port;

import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanQuoteRepository {
    LoanQuoteRecord upsert(LoanQuoteInsert command, List<LoanQuoteTermInsert> terms);

    Optional<LoanQuoteRecord> findByQuoteNo(String quoteNo);

    int countTermsByQuoteId(long quoteId);

    record LoanQuoteInsert(
            String quoteNo,
            Long profileId,
            long creditApplicationId,
            String mobileNo,
            Long couponId,
            Long externalInteractionId,
            LoanTrialQuoteDetail quote,
            Instant quotedAt
    ) {
    }

    record LoanQuoteTermInsert(
            String mobileNo,
            LenderTrialTerm term
    ) {
    }

    record LoanQuoteRecord(
            long id,
            String quoteNo,
            Long profileId,
            long creditApplicationId,
            String mobileNo,
            Long couponId,
            Long externalInteractionId,
            LoanTrialQuoteDetail quote,
            Instant quotedAt
    ) {
        public String productCode() {
            return quote.productCode();
        }

        public String repayMethod() {
            return quote.repayMethod();
        }

        public BigDecimal applyAmt() {
            return quote.applyAmt();
        }

        public BigDecimal loanPrincipal() {
            return quote.loanPrincipal();
        }

        public BigDecimal payAmount() {
            return quote.payAmount();
        }

        public BigDecimal schdAmount() {
            return quote.schdAmount();
        }

        public BigDecimal interest() {
            return quote.interest();
        }
    }
}
