package com.pk.infra.loan;

import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.loan.repository.LoanQuoteInsertParam;
import com.pk.infra.loan.repository.LoanQuoteRow;
import com.pk.infra.loan.repository.LoanQuoteTermInsertParam;
import com.pk.infra.loan.repository.LoanQuoteTermRow;

public final class LoanQuotePersistenceMapper {
    private LoanQuotePersistenceMapper() {
    }

    public static LoanQuoteRepository.LoanQuoteInsert toInsert(
            String quoteNo,
            long creditApplicationId,
            String mobileNo,
            Long productListId,
            LoanTrialQuoteDetail quote,
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            String rawResponseJson,
            java.time.Instant quotedAt
    ) {
        return new LoanQuoteRepository.LoanQuoteInsert(
                quoteNo,
                creditApplicationId,
                mobileNo,
                productListId,
                quote,
                lastLenderRequestJson,
                lastLenderResponseJson,
                rawResponseJson,
                quotedAt
        );
    }

    public static void fillInsertParam(LoanQuoteInsertParam param, LoanQuoteRepository.LoanQuoteInsert command) {
        LoanTrialQuoteDetail quote = command.quote();
        param.setQuoteNo(command.quoteNo());
        param.setCreditApplicationId(command.creditApplicationId());
        param.setMobileNo(command.mobileNo());
        param.setProductListId(command.productListId());
        param.setApplyId(quote.applyId());
        param.setCreditApplyNo(quote.creditApplyNo());
        param.setUserId(quote.userId());
        param.setProductCode(quote.productCode());
        param.setRepayMethod(quote.repayMethod());
        param.setApplyAmt(quote.applyAmt());
        param.setLoanTerm(quote.loanTerm());
        param.setLoanPrincipal(quote.loanPrincipal());
        param.setShowLoanPrincipal(quote.showLoanPrincipal());
        param.setPayAmount(quote.payAmount());
        param.setHandFee(quote.handFee());
        param.setSchdAmount(quote.schdAmount());
        param.setShouldAmount(quote.shouldAmount());
        param.setInterest(quote.interest());
        param.setDayRate(quote.dayRate());
        param.setShowDayRate(quote.showDayRate());
        param.setTotalDays(toTotalDaysInt(quote.totalDays()));
        param.setFee1Name(quote.fee1Name());
        param.setFee1(quote.fee1());
        param.setFee2Name(quote.fee2Name());
        param.setFee2(quote.fee2());
        param.setFee3Name(quote.fee3Name());
        param.setFee3(quote.fee3());
        param.setTax(quote.tax());
        param.setTaxRatePercent(quote.taxRatePercent());
        param.setPpnAmount(quote.ppnAmount());
        param.setShouldStampDuty(quote.shouldStampDuty());
        param.setShouldPrincipal(quote.shouldPrincipal());
        param.setShouldInterest(quote.shouldInterest());
        param.setShouldFee1(quote.shouldFee1());
        param.setShouldFee2(quote.shouldFee2());
        param.setShouldFee3(quote.shouldFee3());
        param.setShouldFee1Tax(quote.shouldFee1Tax());
        param.setShouldFee2Tax(quote.shouldFee2Tax());
        param.setShouldFee3Tax(quote.shouldFee3Tax());
        param.setReductionAmount(quote.reductionAmount());
        param.setAdReductionAmt(quote.adReductionAmt());
        param.setAdReductionRatio(quote.adReductionRatio());
        param.setAdReductionEndDate(quote.adReductionEndDate());
        param.setReductionStampDuty(quote.reductionStampDuty());
        param.setReductionPrincipal(quote.reductionPrincipal());
        param.setReductionInterest(quote.reductionInterest());
        param.setReductionFee1(quote.reductionFee1());
        param.setReductionFee2(quote.reductionFee2());
        param.setReductionFee3(quote.reductionFee3());
        param.setReductionFee1Tax(quote.reductionFee1Tax());
        param.setReductionFee2Tax(quote.reductionFee2Tax());
        param.setReductionFee3Tax(quote.reductionFee3Tax());
        param.setAfterServiceFeeStatus(quote.afterServiceFeeStatus());
        param.setFeePrepayType(quote.feePrepayType());
        param.setLendingDate(quote.lendingDate());
        param.setFirstRepayDate(quote.firstRepayDate());
        param.setLastRepayDate(quote.lastRepayDate());
        param.setUnevenBillsFlag(quote.unevenBillsFlag());
        param.setLastLenderRequestJson(command.lastLenderRequestJson());
        param.setLastLenderResponseJson(command.lastLenderResponseJson());
        param.setRawResponseJson(command.rawResponseJson());
        param.setQuotedAt(command.quotedAt());
    }

    public static void fillTermInsertParam(long quoteId, LoanQuoteTermInsertParam param, LoanQuoteRepository.LoanQuoteTermInsert term) {
        LenderTrialTerm detail = term.term();
        param.setQuoteId(quoteId);
        param.setMobileNo(term.mobileNo());
        param.setTermNo(detail.termNo());
        param.setValueDate(detail.valueDate());
        param.setDueDate(detail.dueDate());
        param.setGraceDate(detail.graceDate());
        param.setSchdAmount(detail.schdAmount());
        param.setSchdPrincipal(detail.schdPrincipal());
        param.setShowLoanPrincipal(detail.showLoanPrincipal());
        param.setSchdInterest(detail.schdInterest());
        param.setShowInterest(detail.showInterest());
        param.setShouldAmount(detail.shouldAmount());
        param.setShouldPrincipal(detail.shouldPrincipal());
        param.setShouldInterest(detail.shouldInterest());
        param.setFee1(detail.fee1());
        param.setFee2(detail.fee2());
        param.setFee3(detail.fee3());
        param.setFee1Tax(detail.fee1Tax());
        param.setFee2Tax(detail.fee2Tax());
        param.setFee3Tax(detail.fee3Tax());
        param.setStampDuty(detail.stampDuty());
        param.setShouldStampDuty(detail.shouldStampDuty());
        param.setReductionAmount(detail.reductionAmount());
        param.setReductionPrincipal(detail.reductionPrincipal());
        param.setReductionInterest(detail.reductionInterest());
        param.setReductionFee1(detail.reductionFee1());
        param.setReductionFee2(detail.reductionFee2());
        param.setReductionFee3(detail.reductionFee3());
        param.setReductionFee1Tax(detail.reductionFee1Tax());
        param.setReductionFee2Tax(detail.reductionFee2Tax());
        param.setReductionFee3Tax(detail.reductionFee3Tax());
        param.setReductionStampDuty(detail.reductionStampDuty());
    }

    public static LoanQuoteRepository.LoanQuoteRecord toRecord(LoanQuoteRow row) {
        return new LoanQuoteRepository.LoanQuoteRecord(
                row.getId(),
                row.getQuoteNo(),
                row.getCreditApplicationId(),
                row.getMobileNo(),
                row.getProductListId(),
                toQuoteDetail(row),
                row.getLastLenderRequestJson(),
                row.getLastLenderResponseJson(),
                row.getRawResponseJson(),
                row.getQuotedAt()
        );
    }

    public static LoanTrialQuoteDetail toQuoteDetail(LoanQuoteRow row) {
        return new LoanTrialQuoteDetail(
                row.getApplyId(),
                row.getCreditApplyNo(),
                row.getUserId(),
                row.getApplyAmt(),
                row.getProductCode(),
                row.getRepayMethod(),
                row.getLoanTerm(),
                row.getLoanPrincipal(),
                row.getShowLoanPrincipal(),
                row.getPayAmount(),
                row.getHandFee(),
                row.getSchdAmount(),
                row.getShouldAmount(),
                row.getInterest(),
                row.getDayRate(),
                row.getShowDayRate(),
                toTotalDaysLong(row.getTotalDays()),
                row.getFee1Name(),
                row.getFee1(),
                row.getFee2Name(),
                row.getFee2(),
                row.getFee3Name(),
                row.getFee3(),
                row.getTax(),
                row.getTaxRatePercent(),
                row.getPpnAmount(),
                row.getShouldStampDuty(),
                row.getShouldPrincipal(),
                row.getShouldInterest(),
                row.getShouldFee1(),
                row.getShouldFee2(),
                row.getShouldFee3(),
                row.getShouldFee1Tax(),
                row.getShouldFee2Tax(),
                row.getShouldFee3Tax(),
                row.getReductionAmount(),
                row.getAdReductionAmt(),
                row.getAdReductionRatio(),
                row.getAdReductionEndDate(),
                row.getReductionStampDuty(),
                row.getReductionPrincipal(),
                row.getReductionInterest(),
                row.getReductionFee1(),
                row.getReductionFee2(),
                row.getReductionFee3(),
                row.getReductionFee1Tax(),
                row.getReductionFee2Tax(),
                row.getReductionFee3Tax(),
                row.getAfterServiceFeeStatus(),
                row.getFeePrepayType(),
                row.getLendingDate(),
                row.getFirstRepayDate(),
                row.getLastRepayDate(),
                row.getUnevenBillsFlag()
        );
    }

    private static Integer toTotalDaysInt(Long totalDays) {
        if (totalDays == null) {
            return null;
        }
        if (totalDays > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return totalDays.intValue();
    }

    private static Long toTotalDaysLong(Integer totalDays) {
        return totalDays == null ? null : totalDays.longValue();
    }
}
