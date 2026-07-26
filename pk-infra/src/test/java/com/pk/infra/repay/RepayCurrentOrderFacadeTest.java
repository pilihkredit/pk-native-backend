package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepayCurrentOrderFacadeTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository;
    @Mock
    private LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort;
    @Mock
    private RepayCurrentOrderRepository repayCurrentOrderRepository;

    private RepayCurrentOrderFacade facade;

    @BeforeEach
    void setUp() {
        facade = new RepayCurrentOrderFacade(
                loanBillReadRepository,
                repaymentTrialSnapshotRepository,
                lenderRepayCurrentOrderPort,
                repayCurrentOrderRepository,
                new ObjectMapper()
        );
    }

    @Test
    void setCurrentOrderCallsLenderAndPersistsActiveRecord() {
        when(loanBillReadRepository.findByUserIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(repaymentTrialSnapshotRepository.findByTrialNo("BT-1")).thenReturn(Optional.of(
                new RepaymentTrialSnapshotRepository.TrialSnapshotRecord(
                        10L,
                        "BT-1",
                        1L,
                        "BATCH",
                        1,
                        null,
                        null,
                        null,
                        99L,
                        Instant.now()
                )
        ));
        when(repayCurrentOrderRepository.upsertActive(any())).thenAnswer(invocation -> {
            RepayCurrentOrderRepository.CurrentOrderUpsert upsert = invocation.getArgument(0);
            return new RepayCurrentOrderRepository.CurrentOrderRecord(
                    99L,
                    upsert.userId(),
                    upsert.currentOrderNo(),
                    upsert.trialId(),
                    upsert.repayOrdersJson(),
                    upsert.couponId(),
                    "ACTIVE",
                    upsert.submittedAt()
            );
        });

        RepayCurrentOrderFacade.CurrentOrderResult result = facade.setCurrentOrder(
                1L,
                "U10001",
                new RepayCurrentOrderFacade.CurrentOrderCommand(
                        "REQ-1",
                        "BT-1",
                        List.of(new RepayCurrentOrderFacade.RepayOrderCommand("LOAN-1", List.of(1)))
                )
        );

        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.requestId()).isEqualTo("REQ-1");
        verify(lenderRepayCurrentOrderPort).setCurrentOrder(any());
        verify(repayCurrentOrderRepository).upsertActive(any());
    }

    @Test
    void setCurrentOrderWithoutBatchTrialNoUsesZeroTrialId() {
        when(loanBillReadRepository.findByUserIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(repayCurrentOrderRepository.upsertActive(any())).thenAnswer(invocation -> {
            RepayCurrentOrderRepository.CurrentOrderUpsert upsert = invocation.getArgument(0);
            assertThat(upsert.trialId()).isZero();
            return new RepayCurrentOrderRepository.CurrentOrderRecord(
                    99L,
                    upsert.userId(),
                    upsert.currentOrderNo(),
                    upsert.trialId(),
                    upsert.repayOrdersJson(),
                    upsert.couponId(),
                    "ACTIVE",
                    upsert.submittedAt()
            );
        });

        RepayCurrentOrderFacade.CurrentOrderResult result = facade.setCurrentOrder(
                1L,
                "U10001",
                new RepayCurrentOrderFacade.CurrentOrderCommand(
                        "REQ-2",
                        null,
                        List.of(new RepayCurrentOrderFacade.RepayOrderCommand("LOAN-1", List.of(1)))
                )
        );

        assertThat(result.status()).isEqualTo("ACTIVE");
        verify(lenderRepayCurrentOrderPort).setCurrentOrder(any());
    }

    private static LoanBillReadRepository.LoanBillRecord loanRecord() {
        return new LoanBillReadRepository.LoanBillRecord(
                100L,
                "LOAN-1",
                "LN-1",
                "BN-1",
                null,
                "DISBURSED"
        );
    }
}
