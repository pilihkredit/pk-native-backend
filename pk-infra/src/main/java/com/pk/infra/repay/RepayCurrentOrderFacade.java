package com.pk.infra.repay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepayCurrentOrderFacade {
    private final LoanBillReadRepository loanBillReadRepository;
    private final RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository;
    private final LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort;
    private final RepayCurrentOrderRepository repayCurrentOrderRepository;
    private final ObjectMapper objectMapper;

    public RepayCurrentOrderFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository,
            LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort,
            RepayCurrentOrderRepository repayCurrentOrderRepository,
            ObjectMapper objectMapper
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.repaymentTrialSnapshotRepository = repaymentTrialSnapshotRepository;
        this.lenderRepayCurrentOrderPort = lenderRepayCurrentOrderPort;
        this.repayCurrentOrderRepository = repayCurrentOrderRepository;
        this.objectMapper = objectMapper;
    }

    public CurrentOrderResult setCurrentOrder(long profileId, String partnerUserId, CurrentOrderCommand command) {
        validateCommand(command);
        Set<String> loanApplyIds = new HashSet<>();
        for (RepayOrderCommand order : command.repayOrders()) {
            if (!loanApplyIds.add(order.loanApplyId())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            loanBillReadRepository.findByProfileIdAndLoanApplyId(profileId, order.loanApplyId())
                    .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        }
        long trialId = resolveTrialId(profileId, command.batchTrialNo());
        List<LenderRepayCurrentOrderPort.RepayOrderItem> lenderOrders = command.repayOrders().stream()
                .map(order -> new LenderRepayCurrentOrderPort.RepayOrderItem(
                        order.loanApplyId(),
                        order.termNos()
                ))
                .toList();
        lenderRepayCurrentOrderPort.setCurrentOrder(new LenderRepayCurrentOrderPort.LenderRepayCurrentOrderCommand(
                partnerUserId,
                lenderOrders,
                null
        ));
        String repayOrdersJson = serializeRepayOrders(command.repayOrders());
        String currentOrderNo = RepayNoGenerator.currentOrderNo();
        Instant submittedAt = Instant.now();
        repayCurrentOrderRepository.upsertActive(new RepayCurrentOrderRepository.CurrentOrderUpsert(
                profileId,
                currentOrderNo,
                trialId,
                repayOrdersJson,
                null,
                submittedAt
        ));
        return new CurrentOrderResult(command.requestId(), "ACTIVE");
    }

    private long resolveTrialId(long profileId, String batchTrialNo) {
        if (batchTrialNo == null || batchTrialNo.isBlank()) {
            return 0L;
        }
        RepaymentTrialSnapshotRepository.TrialSnapshotRecord snapshot = repaymentTrialSnapshotRepository
                .findByTrialNo(batchTrialNo)
                .filter(record -> record.profileId() == profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        return snapshot.id();
    }

    private String serializeRepayOrders(List<RepayOrderCommand> repayOrders) {
        try {
            return objectMapper.writeValueAsString(repayOrders);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static void validateCommand(CurrentOrderCommand command) {
        if (command == null
                || command.requestId() == null
                || command.requestId().isBlank()
                || command.requestId().length() > 64
                || command.repayOrders() == null
                || command.repayOrders().isEmpty()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        for (RepayOrderCommand order : command.repayOrders()) {
            if (order.loanApplyId() == null
                    || order.loanApplyId().isBlank()
                    || order.loanApplyId().length() > 64
                    || order.termNos() == null
                    || order.termNos().isEmpty()) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
        }
    }

    public record CurrentOrderCommand(
            String requestId,
            String batchTrialNo,
            List<RepayOrderCommand> repayOrders
    ) {
    }

    public record RepayOrderCommand(String loanApplyId, List<Integer> termNos) {
    }

    public record CurrentOrderResult(String requestId, String status) {
    }
}
