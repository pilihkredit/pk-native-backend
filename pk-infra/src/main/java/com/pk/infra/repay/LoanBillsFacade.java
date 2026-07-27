package com.pk.infra.repay;

import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.RepayBillStatus;
import com.pk.core.repay.port.LenderLoanBillListPort;
import com.pk.core.repay.port.LoanLenderBillRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class LoanBillsFacade {
    private final LenderLoanBillListPort lenderLoanBillListPort;
    private final LoanLenderBillRepository loanLenderBillRepository;

    public LoanBillsFacade(
            LenderLoanBillListPort lenderLoanBillListPort,
            LoanLenderBillRepository loanLenderBillRepository
    ) {
        this.lenderLoanBillListPort = lenderLoanBillListPort;
        this.loanLenderBillRepository = loanLenderBillRepository;
    }

    public BillsResult listBills(long userId, String partnerUserId, String mobileNo, String status) {
        List<String> billStatuses = resolveBillStatuses(status);
        LenderLoanBillListPort.LenderLoanBillListResult result =
                lenderLoanBillListPort.listBills(partnerUserId, billStatuses);
        Instant queriedAt = Instant.now();
        List<BillResult> bills = result.bills().stream()
                .map(bill -> {
                    persist(userId, result.externalInteractionId(), bill, queriedAt);
                    return toBillResult(bill);
                })
                .toList();
        return new BillsResult(bills);
    }

    private void persist(
            long userId,
            Long externalInteractionId,
            LenderLoanBillListPort.LenderLoanBill bill,
            Instant queriedAt
    ) {
        loanLenderBillRepository.upsert(new LoanLenderBillRepository.LoanLenderBillData(
                bill.loanApplyId(),
                userId,
                bill.loanApplyNo(),
                bill.lenderUserId(),
                bill.billNo(),
                bill.applyAmt(),
                bill.billStatus(),
                bill.termDueDate(),
                bill.nextDueAmount(),
                externalInteractionId,
                queriedAt
        ));
    }

    private static BillResult toBillResult(LenderLoanBillListPort.LenderLoanBill bill) {
        Long nextDueDate = bill.termDueDate();
        BigDecimal nextDueAmount = bill.nextDueAmount();
        String billStatus = bill.billStatus();
        return new BillResult(
                bill.loanApplyId(),
                bill.loanApplyNo(),
                bill.billNo(),
                bill.applyAmt(),
                DisplayFormatters.formatIdrAmount(bill.applyAmt()),
                billStatus,
                nextDueDate,
                DisplayFormatters.formatJakartaDate(nextDueDate),
                nextDueAmount,
                DisplayFormatters.formatIdrAmount(nextDueAmount),
                RepayBillStatus.SETTLE.equals(billStatus)
        );
    }

    private static List<String> resolveBillStatuses(String status) {
        if ("SETTLED".equalsIgnoreCase(status)) {
            return List.of(RepayBillStatus.SETTLE);
        }
        return List.of(RepayBillStatus.NORMAL, RepayBillStatus.OVERDUE);
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
