package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayTrialTerm;
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
        facade = new RepayTrialFacade(
                loanBillReadRepository,
                lenderRepayTrialPort,
                repaymentTrialSnapshotRepository,
                properties,
                new ObjectMapper()
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
                    insert.defaultVaJson(),
                    insert.rawResponseJson(),
                    Instant.now()
            );
        });

        RepayTrialFacade.TrialResult result = facade.trial(
                1L,
                new RepayTrialFacade.TrialCommand("REQ-1", "LOAN-1", List.of(1), "NORMAL")
        );

        assertThat(result.trialNo()).startsWith("RT");
        assertThat(result.repayAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.principalDisplay()).isEqualTo("Rp 250.000");
        assertThat(result.expiresAt()).isGreaterThan(Instant.now().toEpochMilli());
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
        return new LenderRepayTrialResult(
                "LOAN-1",
                "LN-1",
                "BN-1",
                "NORMAL",
                null,
                null,
                null,
                null,
                new BigDecimal("1500000"),
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("295000"),
                new BigDecimal("295000"),
                new BigDecimal("250000"),
                new BigDecimal("45000"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                new BigDecimal("0"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new LenderRepayTrialTerm(
                        "BN-1",
                        "LN-1",
                        1,
                        "N",
                        new BigDecimal("295000"),
                        Instant.parse("2025-06-13T00:00:00Z"),
                        0,
                        null,
                        "NORMAL",
                        10L,
                        new BigDecimal("295000"),
                        new BigDecimal("250000"),
                        new BigDecimal("45000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "N",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "{}"
                )),
                "{}"
        );
    }
}
