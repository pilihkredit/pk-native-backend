package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanAmountValidator;
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

        Instant quotedAt = Instant.now();
        String quoteNo = LoanQuoteNoGenerator.generate();
        List<LoanQuoteRepository.LoanQuoteTermInsert> termInserts = lenderResult.termInfo().stream()
                .map(this::toTermInsert)
                .toList();
        loanQuoteRepository.insert(
                new LoanQuoteRepository.LoanQuoteInsert(
                        quoteNo,
                        creditRecord.id(),
                        productSnapshot.snapshotId(),
                        command.productCode(),
                        command.repayMethod(),
                        lenderResult.applyAmt() == null ? applyAmt : lenderResult.applyAmt(),
                        lenderResult.loanPrincipal(),
                        lenderResult.payAmount(),
                        lenderResult.schdAmount(),
                        lenderResult.interest(),
                        lenderResult.totalDays(),
                        null,
                        lenderResult.rawResponseJson(),
                        quotedAt
                ),
                termInserts
        );

        long expiresAt = quotedAt.plus(loanQuoteProperties.ttl()).toEpochMilli();
        return new TrialResult(
                quoteNo,
                expiresAt,
                lenderResult.applyAmt() == null ? applyAmt : lenderResult.applyAmt(),
                lenderResult.payAmount(),
                lenderResult.schdAmount(),
                lenderResult.interest(),
                lenderResult.loanTerm(),
                lenderResult.termInfo().stream().map(this::toTermResult).toList()
        );
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

    private LoanQuoteRepository.LoanQuoteTermInsert toTermInsert(LenderTrialTerm term) {
        return new LoanQuoteRepository.LoanQuoteTermInsert(
                term.termNo(),
                term.dueDate(),
                term.schdAmount(),
                term.schdPrincipal(),
                term.schdInterest(),
                null
        );
    }

    private TermResult toTermResult(LenderTrialTerm term) {
        Long dueDateMillis = term.dueDate() == null ? null : term.dueDate().toEpochMilli();
        return new TermResult(
                term.termNo(),
                DisplayFormatters.formatTermNo(term.termNo()),
                dueDateMillis,
                DisplayFormatters.formatJakartaDate(dueDateMillis),
                term.schdAmount(),
                DisplayFormatters.formatIdrAmount(term.schdAmount()),
                term.schdPrincipal(),
                DisplayFormatters.formatIdrAmount(term.schdPrincipal()),
                term.schdInterest(),
                DisplayFormatters.formatIdrAmount(term.schdInterest())
        );
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
            BigDecimal applyAmt,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            Integer loanTerm,
            List<TermResult> termInfo
    ) {
    }

    public record TermResult(
            int termNo,
            String termNoDisplay,
            Long dueDate,
            String dueDateDisplay,
            BigDecimal schdAmount,
            String schdAmountDisplay,
            BigDecimal schdPrincipal,
            String schdPrincipalDisplay,
            BigDecimal schdInterest,
            String schdInterestDisplay
    ) {
    }
}
