package com.pk.infra.repay;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.LenderRepayPlanTerm;
import com.pk.core.repay.RepayBillStatus;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class RepayPlanFacade {
    private final LoanBillReadRepository loanBillReadRepository;
    private final RepaymentPlanTermRepository repaymentPlanTermRepository;
    private final LenderRepayPlanPort lenderRepayPlanPort;

    public RepayPlanFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            LenderRepayPlanPort lenderRepayPlanPort
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.repaymentPlanTermRepository = repaymentPlanTermRepository;
        this.lenderRepayPlanPort = lenderRepayPlanPort;
    }

    public List<PlanResult> getPlan(long profileId, String loanApplyId) {
        List<LoanBillReadRepository.LoanBillRecord> loans = resolveLoans(profileId, loanApplyId);
        if (loans.isEmpty()) {
            if (loanApplyId != null && !loanApplyId.isBlank()) {
                throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
            }
            return List.of();
        }
        return loans.stream().map(this::syncAndBuildPlan).toList();
    }

    public void syncPlan(long profileId, String loanApplyId) {
        LoanBillReadRepository.LoanBillRecord loan = loanBillReadRepository
                .findByProfileIdAndLoanApplyId(profileId, loanApplyId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        syncFromLender(loan);
    }

    private List<LoanBillReadRepository.LoanBillRecord> resolveLoans(long profileId, String loanApplyId) {
        if (loanApplyId != null && !loanApplyId.isBlank()) {
            return loanBillReadRepository.findByProfileIdAndLoanApplyId(profileId, loanApplyId)
                    .map(List::of)
                    .orElse(List.of());
        }
        return loanBillReadRepository.findPendingByProfileId(profileId);
    }

    private PlanResult syncAndBuildPlan(LoanBillReadRepository.LoanBillRecord loan) {
        syncFromLender(loan);
        List<RepaymentPlanTermRepository.TermRecord> terms =
                repaymentPlanTermRepository.findByLoanApplicationId(loan.loanApplicationId());
        return new PlanResult(
                loan.loanApplyId(),
                loan.billNo(),
                terms.stream().map(this::toTermResult).toList()
        );
    }

    private void syncFromLender(LoanBillReadRepository.LoanBillRecord loan) {
        LenderRepayPlanPort.LenderRepayPlanResult lenderPlan = lenderRepayPlanPort.fetchPlan(loan.loanApplyId());
        Instant syncedAt = Instant.now();
        List<RepaymentPlanTermRepository.TermUpsert> upserts = lenderPlan.terms().stream()
                .map(this::toUpsert)
                .toList();
        repaymentPlanTermRepository.upsertTerms(
                loan.loanApplicationId(),
                lenderPlan.loanApplyId(),
                lenderPlan.billNo(),
                upserts,
                lenderPlan.requestJson(),
                lenderPlan.rawResponseJson(),
                syncedAt
        );
    }

    private RepaymentPlanTermRepository.TermUpsert toUpsert(LenderRepayPlanTerm term) {
        String mappedStatus = RepayTermStatus.fromLenderStatus(term.termStatus());
        if (mappedStatus == null) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new RepaymentPlanTermRepository.TermUpsert(
                term.termNo(),
                term.subBillNo(),
                mappedStatus,
                term.dueDate(),
                term.graceDate(),
                term.schdAmount(),
                term.shouldAmount(),
                term.paidAmount(),
                term.overdueDays(),
                term.amountDetailJson(),
                term.lastRepayTime()
        );
    }

    private TermResult toTermResult(RepaymentPlanTermRepository.TermRecord term) {
        Long dueDateMillis = term.dueDate() == null ? null : term.dueDate().toEpochMilli();
        return new TermResult(
                term.termNo(),
                DisplayFormatters.formatTermNo(term.termNo()),
                dueDateMillis,
                DisplayFormatters.formatJakartaDate(dueDateMillis),
                term.termStatus(),
                term.shouldAmount(),
                DisplayFormatters.formatIdrAmount(term.shouldAmount()),
                term.amountDetailJson() == null ? null : extractDecimal(term.amountDetailJson(), "shouldPrincipal"),
                term.amountDetailJson() == null ? null : extractDecimal(term.amountDetailJson(), "shouldInterest"),
                term.overdueDays()
        );
    }

    private static BigDecimal extractDecimal(String json, String field) {
        int index = json.indexOf('"' + field + '"');
        if (index < 0) {
            return null;
        }
        int colon = json.indexOf(':', index);
        if (colon < 0) {
            return null;
        }
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && "0123456789.-".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        if (start == end) {
            return null;
        }
        return new BigDecimal(json.substring(start, end));
    }

    public record PlanResult(
            String loanApplyId,
            String billNo,
            List<TermResult> terms
    ) {
    }

    public record TermResult(
            int termNo,
            String termNoDisplay,
            Long dueDate,
            String dueDateDisplay,
            String termStatus,
            BigDecimal shouldAmount,
            String shouldAmountDisplay,
            BigDecimal shouldPrincipal,
            BigDecimal shouldInterest,
            Integer overdueDays
    ) {
    }

    static String deriveBillStatus(List<RepaymentPlanTermRepository.TermRecord> terms) {
        boolean hasOverdue = false;
        boolean allPaid = !terms.isEmpty();
        for (RepaymentPlanTermRepository.TermRecord term : terms) {
            if (RepayTermStatus.OVERDUE.equals(term.termStatus())) {
                hasOverdue = true;
            }
            if (!RepayTermStatus.PAID.equals(term.termStatus())) {
                allPaid = false;
            }
        }
        if (allPaid) {
            return RepayBillStatus.SETTLE;
        }
        if (hasOverdue) {
            return RepayBillStatus.OVERDUE;
        }
        return RepayBillStatus.NORMAL;
    }

    static RepaymentPlanTermRepository.TermRecord findNextDueTerm(
            List<RepaymentPlanTermRepository.TermRecord> terms
    ) {
        return terms.stream()
                .filter(term -> RepayTermStatus.isPending(term.termStatus()))
                .min(Comparator.comparing(
                        RepaymentPlanTermRepository.TermRecord::dueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).thenComparingInt(RepaymentPlanTermRepository.TermRecord::termNo))
                .orElse(null);
    }
}
