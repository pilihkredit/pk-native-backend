package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanAmountValidator;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.loan.port.LoanQuoteRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class LoanTrialFacade {
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    private final LoanProductFacade loanProductFacade;
    private final LenderLoanTrialPort lenderLoanTrialPort;
    private final LoanQuoteRepository loanQuoteRepository;
    private final LoanQuoteProperties loanQuoteProperties;

    public LoanTrialFacade(
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            LoanProductFacade loanProductFacade,
            LenderLoanTrialPort lenderLoanTrialPort,
            LoanQuoteRepository loanQuoteRepository,
            LoanQuoteProperties loanQuoteProperties
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
        this.loanProductFacade = loanProductFacade;
        this.lenderLoanTrialPort = lenderLoanTrialPort;
        this.loanQuoteRepository = loanQuoteRepository;
        this.loanQuoteProperties = loanQuoteProperties;
    }

    public TrialResult trial(long profileId, TrialCommand command) {
        validateCommand(command);
        CreditApplicationRepository.CreditApplicationRecord creditRecord = creditApplicationRepository
                .findByApplyIdAndProfileId(command.applyId(), profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        if (!CreditApplicationStatus.APPROVED.equals(creditRecord.status())) {
            throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
        }

        CreditLenderStatusQueryRepository.CreditLenderStatusQueryData limits = creditLenderStatusQueryRepository
                .findByApplyIdAndProfileId(command.applyId(), profileId)
                .orElseThrow(() -> new ApiException(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE));

        BigDecimal applyAmt = LoanAmountValidator.normalize(command.applyAmt());
        LoanAmountValidator.validateAgainstCreditLimits(
                applyAmt,
                limits.riskMinLimit(),
                limits.fakeCreditLimit(),
                limits.borrowAmtStepSize()
        );

        ProductListResolver.ResolvedProductList productSnapshot = loanProductFacade.resolveProductList(
                profileId,
                command.applyId(),
                true
        );
        loanProductFacade.requireProductRepayMethod(
                productSnapshot,
                command.productCode(),
                command.repayMethod()
        );

        LenderLoanTrialPort.LenderLoanTrialResult lenderResult = lenderLoanTrialPort.trial(
                new LenderLoanTrialPort.LenderLoanTrialCommand(
                        command.applyId(),
                        applyAmt,
                        command.productCode(),
                        command.repayMethod(),
                        command.couponId()
                )
        );

        LoanTrialQuoteDetail quote = normalizeQuote(lenderResult.quote(), command, applyAmt);
        Instant quotedAt = Instant.now();
        String quoteNo = LoanQuoteNoGenerator.generate();
        List<LoanQuoteRepository.LoanQuoteTermInsert> termInserts = lenderResult.termInfo().stream()
                .map(term -> new LoanQuoteRepository.LoanQuoteTermInsert(creditRecord.mobileNo(), term))
                .toList();
        loanQuoteRepository.insert(
                LoanQuotePersistenceMapper.toInsert(
                        quoteNo,
                        creditRecord.id(),
                        creditRecord.mobileNo(),
                        productSnapshot.snapshotId(),
                        quote,
                        lenderResult.requestJson(),
                        lenderResult.rawResponseJson(),
                        lenderResult.rawResponseJson(),
                        quotedAt
                ),
                termInserts
        );

        long expiresAt = quotedAt.plus(loanQuoteProperties.ttl()).toEpochMilli();
        return new TrialResult(
                quoteNo,
                expiresAt,
                quote,
                lenderResult.termInfo().stream().map(this::toTermResult).toList()
        );
    }

    private static LoanTrialQuoteDetail normalizeQuote(
            LoanTrialQuoteDetail quote,
            TrialCommand command,
            BigDecimal normalizedApplyAmt
    ) {
        if (quote == null) {
            return new LoanTrialQuoteDetail(
                    command.applyId(), null, null, normalizedApplyAmt, command.productCode(), command.repayMethod(),
                    null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null
            );
        }
        if (quote.applyAmt() == null || quote.productCode() == null || quote.repayMethod() == null) {
            return new LoanTrialQuoteDetail(
                    quote.applyId() == null ? command.applyId() : quote.applyId(),
                    quote.creditApplyNo(),
                    quote.userId(),
                    normalizedApplyAmt,
                    quote.productCode() == null ? command.productCode() : quote.productCode(),
                    quote.repayMethod() == null ? command.repayMethod() : quote.repayMethod(),
                    quote.loanTerm(),
                    quote.loanPrincipal(),
                    quote.showLoanPrincipal(),
                    quote.payAmount(),
                    quote.handFee(),
                    quote.schdAmount(),
                    quote.shouldAmount(),
                    quote.interest(),
                    quote.dayRate(),
                    quote.showDayRate(),
                    quote.totalDays(),
                    quote.fee1Name(),
                    quote.fee1(),
                    quote.fee2Name(),
                    quote.fee2(),
                    quote.fee3Name(),
                    quote.fee3(),
                    quote.tax(),
                    quote.taxRatePercent(),
                    quote.ppnAmount(),
                    quote.shouldStampDuty(),
                    quote.shouldPrincipal(),
                    quote.shouldInterest(),
                    quote.shouldFee1(),
                    quote.shouldFee2(),
                    quote.shouldFee3(),
                    quote.shouldFee1Tax(),
                    quote.shouldFee2Tax(),
                    quote.shouldFee3Tax(),
                    quote.reductionAmount(),
                    quote.adReductionAmt(),
                    quote.adReductionRatio(),
                    quote.adReductionEndDate(),
                    quote.reductionStampDuty(),
                    quote.reductionPrincipal(),
                    quote.reductionInterest(),
                    quote.reductionFee1(),
                    quote.reductionFee2(),
                    quote.reductionFee3(),
                    quote.reductionFee1Tax(),
                    quote.reductionFee2Tax(),
                    quote.reductionFee3Tax(),
                    quote.afterServiceFeeStatus(),
                    quote.feePrepayType(),
                    quote.lendingDate(),
                    quote.firstRepayDate(),
                    quote.lastRepayDate(),
                    quote.unevenBillsFlag()
            );
        }
        return quote;
    }

    private static void validateCommand(TrialCommand command) {
        if (command == null
                || command.requestId() == null
                || command.requestId().isBlank()
                || command.requestId().length() > 64
                || command.applyId() == null
                || command.applyId().isBlank()
                || command.productCode() == null
                || command.productCode().isBlank()
                || command.productCode().length() > 64
                || command.repayMethod() == null
                || command.repayMethod().isBlank()
                || command.repayMethod().length() > 64
                || command.applyAmt() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private TermResult toTermResult(LenderTrialTerm term) {
        return new TermResult(
                term.termNo(),
                DisplayFormatters.formatTermNo(term.termNo()),
                toMillis(term.valueDate()),
                DisplayFormatters.formatJakartaDate(toMillis(term.valueDate())),
                toMillis(term.dueDate()),
                DisplayFormatters.formatJakartaDate(toMillis(term.dueDate())),
                toMillis(term.graceDate()),
                DisplayFormatters.formatJakartaDate(toMillis(term.graceDate())),
                term.schdAmount(),
                DisplayFormatters.formatIdrAmount(term.schdAmount()),
                term.schdPrincipal(),
                DisplayFormatters.formatIdrAmount(term.schdPrincipal()),
                term.showLoanPrincipal(),
                DisplayFormatters.formatIdrAmount(term.showLoanPrincipal()),
                term.schdInterest(),
                DisplayFormatters.formatIdrAmount(term.schdInterest()),
                term.showInterest(),
                DisplayFormatters.formatIdrAmount(term.showInterest()),
                term.shouldAmount(),
                DisplayFormatters.formatIdrAmount(term.shouldAmount()),
                term.shouldPrincipal(),
                DisplayFormatters.formatIdrAmount(term.shouldPrincipal()),
                term.shouldInterest(),
                DisplayFormatters.formatIdrAmount(term.shouldInterest()),
                term.fee1(),
                DisplayFormatters.formatIdrAmount(term.fee1()),
                term.fee2(),
                DisplayFormatters.formatIdrAmount(term.fee2()),
                term.fee3(),
                DisplayFormatters.formatIdrAmount(term.fee3()),
                term.fee1Tax(),
                DisplayFormatters.formatIdrAmount(term.fee1Tax()),
                term.fee2Tax(),
                DisplayFormatters.formatIdrAmount(term.fee2Tax()),
                term.fee3Tax(),
                DisplayFormatters.formatIdrAmount(term.fee3Tax()),
                term.stampDuty(),
                DisplayFormatters.formatIdrAmount(term.stampDuty()),
                term.shouldStampDuty(),
                DisplayFormatters.formatIdrAmount(term.shouldStampDuty()),
                term.reductionAmount(),
                DisplayFormatters.formatIdrAmount(term.reductionAmount()),
                term.reductionPrincipal(),
                DisplayFormatters.formatIdrAmount(term.reductionPrincipal()),
                term.reductionInterest(),
                DisplayFormatters.formatIdrAmount(term.reductionInterest()),
                term.reductionFee1(),
                DisplayFormatters.formatIdrAmount(term.reductionFee1()),
                term.reductionFee2(),
                DisplayFormatters.formatIdrAmount(term.reductionFee2()),
                term.reductionFee3(),
                DisplayFormatters.formatIdrAmount(term.reductionFee3()),
                term.reductionFee1Tax(),
                DisplayFormatters.formatIdrAmount(term.reductionFee1Tax()),
                term.reductionFee2Tax(),
                DisplayFormatters.formatIdrAmount(term.reductionFee2Tax()),
                term.reductionFee3Tax(),
                DisplayFormatters.formatIdrAmount(term.reductionFee3Tax()),
                term.reductionStampDuty(),
                DisplayFormatters.formatIdrAmount(term.reductionStampDuty())
        );
    }

    private static Long toMillis(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    public record TrialCommand(
            String requestId,
            String applyId,
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId
    ) {
    }

    public record TrialResult(
            String quoteNo,
            long expiresAt,
            LoanTrialQuoteDetail quote,
            List<TermResult> termInfo
    ) {
    }

    public record TermResult(
            int termNo,
            String termNoDisplay,
            Long valueDate,
            String valueDateDisplay,
            Long dueDate,
            String dueDateDisplay,
            Long graceDate,
            String graceDateDisplay,
            BigDecimal schdAmount,
            String schdAmountDisplay,
            BigDecimal schdPrincipal,
            String schdPrincipalDisplay,
            BigDecimal showLoanPrincipal,
            String showLoanPrincipalDisplay,
            BigDecimal schdInterest,
            String schdInterestDisplay,
            BigDecimal showInterest,
            String showInterestDisplay,
            BigDecimal shouldAmount,
            String shouldAmountDisplay,
            BigDecimal shouldPrincipal,
            String shouldPrincipalDisplay,
            BigDecimal shouldInterest,
            String shouldInterestDisplay,
            BigDecimal fee1,
            String fee1Display,
            BigDecimal fee2,
            String fee2Display,
            BigDecimal fee3,
            String fee3Display,
            BigDecimal fee1Tax,
            String fee1TaxDisplay,
            BigDecimal fee2Tax,
            String fee2TaxDisplay,
            BigDecimal fee3Tax,
            String fee3TaxDisplay,
            BigDecimal stampDuty,
            String stampDutyDisplay,
            BigDecimal shouldStampDuty,
            String shouldStampDutyDisplay,
            BigDecimal reductionAmount,
            String reductionAmountDisplay,
            BigDecimal reductionPrincipal,
            String reductionPrincipalDisplay,
            BigDecimal reductionInterest,
            String reductionInterestDisplay,
            BigDecimal reductionFee1,
            String reductionFee1Display,
            BigDecimal reductionFee2,
            String reductionFee2Display,
            BigDecimal reductionFee3,
            String reductionFee3Display,
            BigDecimal reductionFee1Tax,
            String reductionFee1TaxDisplay,
            BigDecimal reductionFee2Tax,
            String reductionFee2TaxDisplay,
            BigDecimal reductionFee3Tax,
            String reductionFee3TaxDisplay,
            BigDecimal reductionStampDuty,
            String reductionStampDutyDisplay
    ) {
    }
}
