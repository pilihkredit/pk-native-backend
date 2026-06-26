package com.pk.infra.repay;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RepayBillsOverviewFacade {
    private final LoanBillReadRepository loanBillReadRepository;
    private final RepaymentPlanTermRepository repaymentPlanTermRepository;
    private final RepayCurrentOrderRepository repayCurrentOrderRepository;
    private final ObjectMapper objectMapper;
    private final Optional<RepayPlanFacade> repayPlanFacade;

    public RepayBillsOverviewFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            RepayCurrentOrderRepository repayCurrentOrderRepository,
            ObjectMapper objectMapper,
            Optional<RepayPlanFacade> repayPlanFacade
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.repaymentPlanTermRepository = repaymentPlanTermRepository;
        this.repayCurrentOrderRepository = repayCurrentOrderRepository;
        this.objectMapper = objectMapper;
        this.repayPlanFacade = repayPlanFacade;
    }

    public OverviewResult getOverview(long profileId) {
        List<LoanBillReadRepository.LoanBillRecord> pendingLoans =
                loanBillReadRepository.findPendingByProfileId(profileId);
        Set<String> selectedLoanApplyIds = loadSelectedLoanApplyIds(profileId);

        BigDecimal totalDueAmount = BigDecimal.ZERO;
        boolean hasOverdue = false;
        List<BillSummaryResult> summaries = new ArrayList<>();
        for (LoanBillReadRepository.LoanBillRecord loan : pendingLoans) {
            repayPlanFacade.ifPresent(facade -> facade.syncPlan(profileId, loan.loanApplyId()));
            List<RepaymentPlanTermRepository.TermRecord> terms =
                    repaymentPlanTermRepository.findByLoanApplicationId(loan.loanApplicationId());
            BigDecimal currentDueAmount = BigDecimal.ZERO;
            int maxOverdueDays = 0;
            for (RepaymentPlanTermRepository.TermRecord term : terms) {
                if (!RepayTermStatus.isPending(term.termStatus())) {
                    continue;
                }
                if (term.shouldAmount() != null) {
                    currentDueAmount = currentDueAmount.add(term.shouldAmount());
                }
                if (RepayTermStatus.OVERDUE.equals(term.termStatus())
                        || (term.overdueDays() != null && term.overdueDays() > 0)) {
                    hasOverdue = true;
                }
                if (term.overdueDays() != null && term.overdueDays() > maxOverdueDays) {
                    maxOverdueDays = term.overdueDays();
                }
            }
            totalDueAmount = totalDueAmount.add(currentDueAmount);
            summaries.add(new BillSummaryResult(
                    loan.loanApplyId(),
                    loan.billNo(),
                    maxOverdueDays,
                    currentDueAmount,
                    DisplayFormatters.formatIdrAmount(currentDueAmount),
                    selectedLoanApplyIds.contains(loan.loanApplyId())
            ));
        }
        return new OverviewResult(
                summaries.size(),
                totalDueAmount,
                DisplayFormatters.formatIdrAmount(totalDueAmount),
                hasOverdue,
                summaries
        );
    }

    private Set<String> loadSelectedLoanApplyIds(long profileId) {
        return repayCurrentOrderRepository.findActiveByProfileId(profileId)
                .map(record -> parseSelectedLoanApplyIds(record.repayOrdersJson()))
                .orElse(Set.of());
    }

    private Set<String> parseSelectedLoanApplyIds(String repayOrdersJson) {
        try {
            List<Map<String, Object>> orders = objectMapper.readValue(
                    repayOrdersJson,
                    new TypeReference<>() {
                    }
            );
            Set<String> selected = new HashSet<>();
            for (Map<String, Object> order : orders) {
                Object loanApplyId = order.get("loanApplyId");
                if (loanApplyId != null) {
                    selected.add(String.valueOf(loanApplyId));
                }
            }
            return selected;
        } catch (Exception exception) {
            return Set.of();
        }
    }

    public record OverviewResult(
            int totalBillCount,
            BigDecimal totalDueAmount,
            String totalDueAmountDisplay,
            boolean hasOverdue,
            List<BillSummaryResult> bills
    ) {
    }

    public record BillSummaryResult(
            String loanApplyId,
            String billNo,
            int overdueDays,
            BigDecimal currentDueAmount,
            String currentDueAmountDisplay,
            boolean selected
    ) {
    }
}
