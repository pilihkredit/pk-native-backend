package com.pk.infra.repay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepayTrialFacade {
    private final LoanBillReadRepository loanBillReadRepository;
    private final LenderRepayTrialPort lenderRepayTrialPort;
    private final RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository;
    private final RepayTrialProperties repayTrialProperties;
    private final ObjectMapper objectMapper;

    public RepayTrialFacade(
            LoanBillReadRepository loanBillReadRepository,
            LenderRepayTrialPort lenderRepayTrialPort,
            RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository,
            RepayTrialProperties repayTrialProperties,
            ObjectMapper objectMapper
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.lenderRepayTrialPort = lenderRepayTrialPort;
        this.repaymentTrialSnapshotRepository = repaymentTrialSnapshotRepository;
        this.repayTrialProperties = repayTrialProperties;
        this.objectMapper = objectMapper;
    }

    public TrialResult trial(long profileId, TrialCommand command) {
        validateTrialCommand(command);
        requireLoan(profileId, command.loanApplyId());
        boolean settle = isEarlySettle(command.repayType(), command.termNos());
        if (!settle && (command.termNos() == null || command.termNos().isEmpty())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        LenderRepayTrialResult lenderResult = lenderRepayTrialPort.trial(
                new LenderRepayTrialPort.LenderRepayTrialCommand(
                        command.loanApplyId(),
                        settle,
                        command.termNos()
                )
        );
        Instant createdAt = Instant.now();
        String trialNo = RepayNoGenerator.trialNo();
        long expiresAt = createdAt.plus(repayTrialProperties.ttl()).toEpochMilli();
        persistSingleTrial(profileId, trialNo, settle, command.termNos(), lenderResult, createdAt);
        return toTrialResult(trialNo, expiresAt, lenderResult);
    }

    public BatchTrialResult trialBatch(long profileId, BatchTrialCommand command) {
        validateBatchCommand(command);
        Set<String> loanApplyIds = new HashSet<>();
        List<LenderRepayTrialPort.LenderRepayTrialCommand> lenderCommands = new ArrayList<>();
        for (BatchOrderCommand order : command.repayOrders()) {
            if (!loanApplyIds.add(order.loanApplyId())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            requireLoan(profileId, order.loanApplyId());
            boolean settle = order.settle();
            if (!settle && (order.termNos() == null || order.termNos().isEmpty())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            lenderCommands.add(new LenderRepayTrialPort.LenderRepayTrialCommand(
                    order.loanApplyId(),
                    settle,
                    order.termNos()
            ));
        }
        LenderRepayTrialPort.LenderRepayTrialBatchResult lenderResult = lenderRepayTrialPort.trialBatch(
                new LenderRepayTrialPort.LenderRepayTrialBatchCommand(lenderCommands)
        );
        Instant createdAt = Instant.now();
        String batchTrialNo = RepayNoGenerator.batchTrialNo();
        persistBatchTrial(profileId, batchTrialNo, command.repayOrders(), lenderResult, createdAt);
        return new BatchTrialResult(
                batchTrialNo,
                lenderResult.totalShouldAmount(),
                DisplayFormatters.formatIdrAmount(lenderResult.totalShouldAmount()),
                lenderResult.defaultVa() == null ? null : toVaSummary(lenderResult.defaultVa()),
                lenderResult.billTrials().stream()
                        .map(trial -> toBillTrialSummary(trial, createdAt))
                        .toList()
        );
    }

    private void requireLoan(long profileId, String loanApplyId) {
        loanBillReadRepository.findByProfileIdAndLoanApplyId(profileId, loanApplyId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
    }

    private void persistSingleTrial(
            long profileId,
            String trialNo,
            boolean settle,
            List<Integer> termNos,
            LenderRepayTrialResult lenderResult,
            Instant createdAt
    ) {
        LoanBillReadRepository.LoanBillRecord loan = loanBillReadRepository
                .findByProfileIdAndLoanApplyId(profileId, lenderResult.loanApplyId())
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        repaymentTrialSnapshotRepository.insert(
                new RepaymentTrialSnapshotRepository.TrialSnapshotInsert(
                        trialNo,
                        profileId,
                        "SINGLE",
                        1,
                        lenderResult.shouldAmount(),
                        lenderResult.reductionAmount(),
                        lenderResult.paidAmount(),
                        lenderResult.defaultVa(),
                        lenderResult.spareVa(),
                        lenderResult.disabledDefaultVa(),
                        lenderResult.rawResponseJson()
                ),
                List.of(new RepaymentTrialSnapshotRepository.TrialOrderInsert(
                        loan.loanApplicationId(),
                        settle,
                        serializeTermNos(termNos),
                        lenderResult
                ))
        );
    }

    private void persistBatchTrial(
            long profileId,
            String batchTrialNo,
            List<BatchOrderCommand> orders,
            LenderRepayTrialPort.LenderRepayTrialBatchResult lenderResult,
            Instant createdAt
    ) {
        List<RepaymentTrialSnapshotRepository.TrialOrderInsert> orderInserts = new ArrayList<>();
        for (LenderRepayTrialResult billTrial : lenderResult.billTrials()) {
            LoanBillReadRepository.LoanBillRecord loan = loanBillReadRepository
                    .findByProfileIdAndLoanApplyId(profileId, billTrial.loanApplyId())
                    .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
            BatchOrderCommand sourceOrder = orders.stream()
                    .filter(order -> order.loanApplyId().equals(billTrial.loanApplyId()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(ApiCode.SERVICE_UNAVAILABLE));
            orderInserts.add(new RepaymentTrialSnapshotRepository.TrialOrderInsert(
                    loan.loanApplicationId(),
                    sourceOrder.settle(),
                    serializeTermNos(sourceOrder.termNos()),
                    billTrial
            ));
        }
        repaymentTrialSnapshotRepository.insert(
                new RepaymentTrialSnapshotRepository.TrialSnapshotInsert(
                        batchTrialNo,
                        profileId,
                        "BATCH",
                        lenderResult.totalBillCount(),
                        lenderResult.totalShouldAmount(),
                        lenderResult.totalReductionAmount(),
                        lenderResult.totalPaidAmount(),
                        lenderResult.defaultVa(),
                        lenderResult.spareVa(),
                        lenderResult.disabledDefaultVa(),
                        lenderResult.rawResponseJson()
                ),
                orderInserts
        );
    }

    private TrialResult toTrialResult(String trialNo, long expiresAt, LenderRepayTrialResult lenderResult) {
        return new TrialResult(trialNo, expiresAt, lenderResult);
    }

    private BillTrialSummary toBillTrialSummary(LenderRepayTrialResult trial, Instant createdAt) {
        return new BillTrialSummary(
                trial.loanApplyId(),
                trial.billNo(),
                trial.shouldAmount(),
                DisplayFormatters.formatIdrAmount(trial.shouldAmount()),
                trial.shouldPrincipal(),
                trial.shouldInterest(),
                trial.shouldPenalty() != null ? trial.shouldPenalty() : trial.shouldPenInterest(),
                trial.shouldFee()
        );
    }

    private VaSummary toVaSummary(LenderRepayVa va) {
        return new VaSummary(va.vaNo(), va.bankCode(), va.bankName(), va.defaultFlag());
    }

    private String serializeTermNos(List<Integer> termNos) {
        try {
            return objectMapper.writeValueAsString(termNos == null ? List.of() : termNos);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static boolean isEarlySettle(String repayType, List<Integer> termNos) {
        if ("EARLY_SETTLE".equalsIgnoreCase(repayType)) {
            return true;
        }
        return termNos == null || termNos.isEmpty();
    }

    private static void validateTrialCommand(TrialCommand command) {
        if (command == null
                || command.requestId() == null
                || command.requestId().isBlank()
                || command.requestId().length() > 64
                || command.loanApplyId() == null
                || command.loanApplyId().isBlank()
                || command.loanApplyId().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static void validateBatchCommand(BatchTrialCommand command) {
        if (command == null
                || command.requestId() == null
                || command.requestId().isBlank()
                || command.requestId().length() > 64
                || command.repayOrders() == null
                || command.repayOrders().isEmpty()
                || command.repayOrders().size() > 20) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        for (BatchOrderCommand order : command.repayOrders()) {
            if (order.loanApplyId() == null
                    || order.loanApplyId().isBlank()
                    || order.loanApplyId().length() > 64
                    || order.settle() == null) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
        }
    }

    public record TrialCommand(
            String requestId,
            String loanApplyId,
            List<Integer> termNos,
            String repayType
    ) {
    }

    public record BatchTrialCommand(String requestId, List<BatchOrderCommand> repayOrders) {
    }

    public record BatchOrderCommand(String loanApplyId, Boolean settle, List<Integer> termNos) {
    }

    public record TrialResult(
            String trialNo,
            long expiresAt,
            LenderRepayTrialResult lender
    ) {
    }

    public record BatchTrialResult(
            String batchTrialNo,
            BigDecimal totalShouldAmount,
            String totalShouldAmountDisplay,
            VaSummary defaultVa,
            List<BillTrialSummary> billTrials
    ) {
    }

    public record VaSummary(String vaNo, String bankChannel, String bankName, boolean defaultFlag) {
    }

    public record BillTrialSummary(
            String loanApplyId,
            String billNo,
            BigDecimal repayAmount,
            String repayAmountDisplay,
            BigDecimal principal,
            BigDecimal interest,
            BigDecimal penalty,
            BigDecimal fee
    ) {
    }
}
