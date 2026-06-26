package com.pk.infra.repay;

import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.RepayBillStatus;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanBillsFacade {
    private final LoanBillReadRepository loanBillReadRepository;
    private final RepaymentPlanTermRepository repaymentPlanTermRepository;
    private final Optional<RepayPlanFacade> repayPlanFacade;

    public LoanBillsFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            Optional<RepayPlanFacade> repayPlanFacade
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.repaymentPlanTermRepository = repaymentPlanTermRepository;
        this.repayPlanFacade = repayPlanFacade;
    }

    public BillsResult listBills(long profileId, String status) {
        LoanBillReadRepository.BillFilter filter = "SETTLED".equalsIgnoreCase(status)
                ? LoanBillReadRepository.BillFilter.SETTLED
                : LoanBillReadRepository.BillFilter.ACTIVE;
        List<LoanBillReadRepository.LoanBillRecord> loans =
                loanBillReadRepository.findByProfileIdAndBillFilter(profileId, filter);
        List<BillResult> bills = new ArrayList<>();
        for (LoanBillReadRepository.LoanBillRecord loan : loans) {
            repayPlanFacade.ifPresent(facade -> facade.syncPlan(profileId, loan.loanApplyId()));
            List<RepaymentPlanTermRepository.TermRecord> terms =
                    repaymentPlanTermRepository.findByLoanApplicationId(loan.loanApplicationId());
            RepaymentPlanTermRepository.TermRecord nextDue = RepayPlanFacade.findNextDueTerm(terms);
            Long nextDueDate = nextDue == null || nextDue.dueDate() == null ? null : nextDue.dueDate().toEpochMilli();
            BigDecimal nextDueAmount = nextDue == null ? null : nextDue.shouldAmount();
            String billStatus = RepayPlanFacade.deriveBillStatus(terms);
            bills.add(new BillResult(
                    loan.loanApplyId(),
                    loan.loanApplyNo(),
                    loan.billNo(),
                    loan.applyAmt(),
                    DisplayFormatters.formatIdrAmount(loan.applyAmt()),
                    billStatus,
                    nextDueDate,
                    DisplayFormatters.formatJakartaDate(nextDueDate),
                    nextDueAmount,
                    DisplayFormatters.formatIdrAmount(nextDueAmount),
                    RepayBillStatus.SETTLE.equals(billStatus)
            ));
        }
        return new BillsResult(bills);
    }

    public record BillsResult(List<BillResult> bills) {
    }

    public record BillResult(
            String loanApplyId,
            String loanApplyNo,
            String billNo,
            BigDecimal applyAmt,
            String applyAmtDisplay,
            String billStatus,
            Long nextDueDate,
            String nextDueDateDisplay,
            BigDecimal nextDueAmount,
            String nextDueAmountDisplay,
            boolean canReloan
    ) {
    }
}
