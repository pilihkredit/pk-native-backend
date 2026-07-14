package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepayTrialFacadeTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private LenderRepayTrialPort lenderRepayTrialPort;
    @Mock
    private RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository;

    private RepayTrialFacade facade;

    @BeforeEach
    void setUp() {
        RepayTrialProperties properties = new RepayTrialProperties();
        properties.setTtl(Duration.ofMinutes(15));
        ObjectMapper objectMapper = new ObjectMapper();
        facade = new RepayTrialFacade(
                loanBillReadRepository,
                lenderRepayTrialPort,
                repaymentTrialSnapshotRepository,
                properties,
                objectMapper
        );
    }

    @Test
    void trialPersistsSnapshotAndReturnsDisplayFields() {
        when(loanBillReadRepository.findByProfileIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(lenderRepayTrialPort.trial(any())).thenReturn(lenderTrialResult());
        when(repaymentTrialSnapshotRepository.insert(any(), any())).thenAnswer(invocation -> {
            RepaymentTrialSnapshotRepository.TrialSnapshotInsert insert = invocation.getArgument(0);
            return new RepaymentTrialSnapshotRepository.TrialSnapshotRecord(
                    10L,
                    insert.trialNo(),
                    insert.profileId(),
                    insert.trialType(),
                    insert.totalBillCount(),
                    insert.totalShouldAmount(),
                    insert.totalReductionAmount(),
                    insert.totalPaidAmount(),
                    insert.rawResponseJson(),
                    Instant.now()
            );
        });

        RepayTrialFacade.TrialResult result = facade.trial(
                1L,
                new RepayTrialFacade.TrialCommand("REQ-1", "LOAN-1", List.of(1), "NORMAL")
        );

        assertThat(result.trialNo()).startsWith("RT");
        assertThat(result.lender().shouldAmount()).isEqualByComparingTo("295000");
        assertThat(result.lender().shouldPrincipal()).isEqualByComparingTo("250000");
        assertThat(result.expiresAt()).isGreaterThan(Instant.now().toEpochMilli());
    }

    @Test
    void trialEarlySettleWhenTermNosOmitted() {
        when(loanBillReadRepository.findByProfileIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(lenderRepayTrialPort.trial(any())).thenAnswer(invocation -> {
            LenderRepayTrialPort.LenderRepayTrialCommand command = invocation.getArgument(0);
            assertThat(command.settle()).isTrue();
            assertThat(command.termNos()).isNullOrEmpty();
            return lenderTrialResult();
        });
        when(repaymentTrialSnapshotRepository.insert(any(), any())).thenAnswer(invocation ->
                new RepaymentTrialSnapshotRepository.TrialSnapshotRecord(
                        10L,
                        ((RepaymentTrialSnapshotRepository.TrialSnapshotInsert) invocation.getArgument(0)).trialNo(),
                        1L,
                        "SINGLE",
                        1,
                        new BigDecimal("295000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "{}",
                        Instant.now()
                ));

        RepayTrialFacade.TrialResult result = facade.trial(
                1L,
                new RepayTrialFacade.TrialCommand("REQ-2", "LOAN-1", null, null)
        );

        assertThat(result.lender().shouldAmount()).isEqualByComparingTo("295000");
    }

    @Test
    void trialBatchPersistsSnapshotAndReturnsAggregatedResult() {
        when(loanBillReadRepository.findByProfileIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(lenderRepayTrialPort.trialBatch(any())).thenReturn(lenderBatchResult());
        when(repaymentTrialSnapshotRepository.insert(any(), any())).thenAnswer(invocation -> {
            RepaymentTrialSnapshotRepository.TrialSnapshotInsert insert = invocation.getArgument(0);
            assertThat(insert.trialType()).isEqualTo("BATCH");
            return new RepaymentTrialSnapshotRepository.TrialSnapshotRecord(
                    11L,
                    insert.trialNo(),
                    insert.profileId(),
                    insert.trialType(),
                    insert.totalBillCount(),
                    insert.totalShouldAmount(),
                    insert.totalReductionAmount(),
                    insert.totalPaidAmount(),
                    insert.rawResponseJson(),
                    Instant.now()
            );
        });

        RepayTrialFacade.BatchTrialResult result = facade.trialBatch(
                1L,
                new RepayTrialFacade.BatchTrialCommand(
                        "REQ-BATCH-1",
                        List.of(new RepayTrialFacade.BatchOrderCommand("LOAN-1", false, List.of(1)))
                )
        );

        assertThat(result.batchTrialNo()).startsWith("BT");
        assertThat(result.totalShouldAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.billTrials()).hasSize(1);
        assertThat(result.billTrials().getFirst().repayAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.defaultVa()).isNotNull();
        assertThat(result.defaultVa().vaNo()).isEqualTo("8801234567890");
    }

    private static LenderRepayTrialPort.LenderRepayTrialBatchResult lenderBatchResult() {
        return new LenderRepayTrialPort.LenderRepayTrialBatchResult(
                1,
                new BigDecimal("295000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new com.pk.core.repay.LenderRepayVa(
                        "8801234567890",
                        "BCA",
                        "Bank Central Asia",
                        null,
                        null,
                        List.of(),
                        true,
                        false,
                        true
                ),
                null,
                null,
                List.of(lenderTrialResult()),
                "{}"
        );
    }

    private static LoanBillReadRepository.LoanBillRecord loanRecord() {
        return new LoanBillReadRepository.LoanBillRecord(
                100L,
                "LOAN-1",
                "LN-1",
                "BN-1",
                new BigDecimal("1500000"),
                "DISBURSED"
        );
    }

    private static LenderRepayTrialResult lenderTrialResult() {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal shouldAmount = new BigDecimal("295000");
        BigDecimal shouldPrincipal = new BigDecimal("250000");
        BigDecimal shouldInterest = new BigDecimal("45000");
        return new LenderRepayTrialResult(
                "LOAN-1",
                "LN-1",
                "BN-1",
                "NORMAL",
                null, null, null, null,
                new BigDecimal("1500000"),
                null, null, null, null, null,
                shouldAmount,
                null, null, null, null, null, null, null, null, null, null, null, null,
                shouldAmount,
                null,
                shouldPrincipal,
                shouldInterest,
                null, null, null, null, null, null, null, null, null,
                zero, zero, zero, zero, zero,
                zero,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null,
                zero,
                null, null, null,
                null, null, null, null,
                null, null,
                List.of(),
                "{}"
        );
    }
}
