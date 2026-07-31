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
                bill.contrNo(),
                bill.terms(),
                bill.applyAmt(),
                bill.currency(),
                bill.userName(),
                bill.statusDate(),
                bill.billStatus(),
                bill.feePrepayAmt(),
                bill.lendAmt(),
                bill.lendTime(),
                bill.paySerialNo(),
                bill.dueDate(),
                bill.stampDuty(),
                bill.principal(),
                bill.interest(),
                bill.fee1(),
                bill.fee2(),
                bill.fee3(),
                bill.fee1Tax(),
                bill.fee2Tax(),
                bill.fee3Tax(),
                bill.prePenInterest(),
                bill.penInterest(),
                bill.initLateFee(),
                bill.termNo(),
                bill.termDueDate(),
                bill.shouldStampDuty(),
                bill.shouldPrincipal(),
                bill.shouldInterest(),
                bill.shouldFee1(),
                bill.shouldFee2(),
                bill.shouldFee3(),
                bill.shouldFee1Tax(),
                bill.shouldFee2Tax(),
                bill.shouldFee3Tax(),
                bill.shouldPenInterest(),
                bill.shouldInitLateFee(),
                bill.paidStampDuty(),
                bill.paidPrincipal(),
                bill.paidInterest(),
                bill.paidFee1(),
                bill.paidFee2(),
                bill.paidFee3(),
                bill.paidFee1Tax(),
                bill.paidFee2Tax(),
                bill.paidFee3Tax(),
                bill.paidPenInterest(),
                bill.paidInitLateFee(),
                bill.paidAdvSettleFee(),
                bill.reductionStampDuty(),
                bill.reductionPrincipal(),
                bill.reductionInterest(),
                bill.reductionFee1(),
                bill.reductionFee2(),
                bill.reductionFee3(),
                bill.reductionFee1Tax(),
                bill.reductionFee2Tax(),
                bill.reductionFee3Tax(),
                bill.reductionPenInterest(),
                bill.reductionInitLateFee(),
                bill.reductionAdvSettleFee(),
                bill.overdueDays(),
                bill.firstOverdueDay(),
                bill.lastRepayTime(),
                bill.maxOverdueDays(),
                bill.paidOutDate(),
                bill.advSetteFlag(),
                externalInteractionId,
                queriedAt
        ));
    }

    private static BillResult toBillResult(LenderLoanBillListPort.LenderLoanBill bill) {
        Long nextDueDate = bill.termDueDate();
        BigDecimal nextDueAmount = bill.sumShouldAmounts();
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
