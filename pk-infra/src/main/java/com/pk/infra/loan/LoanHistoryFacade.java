package com.pk.infra.loan;

import com.pk.core.loan.port.LenderLoanHistoryPort;
import com.pk.core.loan.port.LoanLenderHistoryOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class LoanHistoryFacade {
    private final LenderLoanHistoryPort lenderLoanHistoryPort;
    private final LoanLenderHistoryOrderRepository loanLenderHistoryOrderRepository;

    public LoanHistoryFacade(
            LenderLoanHistoryPort lenderLoanHistoryPort,
            LoanLenderHistoryOrderRepository loanLenderHistoryOrderRepository
    ) {
        this.lenderLoanHistoryPort = lenderLoanHistoryPort;
        this.loanLenderHistoryOrderRepository = loanLenderHistoryOrderRepository;
    }

    public HistoryResult listHistory(long profileId, String partnerUserId, String mobileNo) {
        LenderLoanHistoryPort.LenderLoanHistoryResult result =
                lenderLoanHistoryPort.queryHistory(partnerUserId);
        Instant queriedAt = Instant.now();
        List<HistoryOrder> orders = result.orders().stream()
                .map(order -> {
                    persist(profileId, mobileNo, result.requestJson(), order, queriedAt);
                    return toHistoryOrder(order);
                })
                .toList();
        return new HistoryResult(orders);
    }

    private void persist(
            long profileId,
            String mobileNo,
            String requestJson,
            LenderLoanHistoryPort.LenderLoanHistoryOrder order,
            Instant queriedAt
    ) {
        loanLenderHistoryOrderRepository.upsert(new LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData(
                order.loanApplyId(),
                profileId,
                mobileNo,
                order.loanApplyNo(),
                order.lenderUserId(),
                order.applyStatus(),
                order.billNo(),
                order.applyAmt(),
                order.payAmount(),
                order.payTime() == null ? null : Instant.ofEpochMilli(order.payTime()),
                order.freezeEndTime(),
                order.createTime(),
                requestJson,
                order.responseItemJson(),
                queriedAt
        ));
    }

    private static HistoryOrder toHistoryOrder(LenderLoanHistoryPort.LenderLoanHistoryOrder order) {
        return new HistoryOrder(
                order.loanApplyId(),
                order.loanApplyNo(),
                order.applyStatus(),
                order.billNo(),
                order.applyAmt(),
                order.payAmount(),
                order.payTime(),
                order.freezeEndTime(),
                order.createTime()
        );
    }

    public record HistoryResult(List<HistoryOrder> orders) {
    }

    public record HistoryOrder(
            String loanApplyId,
            String loanApplyNo,
            String externalStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime,
            Long freezeEndTime,
            Long createTime
    ) {
    }
}
