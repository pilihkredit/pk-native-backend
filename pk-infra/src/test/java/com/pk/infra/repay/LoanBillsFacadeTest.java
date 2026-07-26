package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.repay.RepayBillStatus;
import com.pk.core.repay.port.LenderLoanBillListPort;
import com.pk.core.repay.port.LoanLenderBillRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanBillsFacadeTest {
    @Mock
    private LenderLoanBillListPort lenderLoanBillListPort;
    @Mock
    private LoanLenderBillRepository loanLenderBillRepository;

    private LoanBillsFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanBillsFacade(lenderLoanBillListPort, loanLenderBillRepository);
    }

    @Test
    void syncsActiveBillsFromLenderAndUpsertsByLoanApplyId() {
        when(lenderLoanBillListPort.listBills("partner-1", List.of("NORMAL", "OVERDUE"))).thenReturn(
                new LenderLoanBillListPort.LenderLoanBillListResult(
                        99L,
                        List.of(bill("LOAN-1", RepayBillStatus.NORMAL))
                )
        );

        LoanBillsFacade.BillsResult result = facade.listBills(1L, "partner-1", "81234567890", "ACTIVE");

        ArgumentCaptor<LoanLenderBillRepository.LoanLenderBillData> captor =
                ArgumentCaptor.forClass(LoanLenderBillRepository.LoanLenderBillData.class);
        verify(loanLenderBillRepository).upsert(captor.capture());

        LoanLenderBillRepository.LoanLenderBillData saved = captor.getValue();
        assertThat(saved.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(saved.userId()).isEqualTo(1L);
        assertThat(saved.billStatus()).isEqualTo(RepayBillStatus.NORMAL);
        assertThat(saved.externalInteractionId()).isEqualTo(99L);

        assertThat(result.bills()).hasSize(1);
        assertThat(result.bills().getFirst().loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.bills().getFirst().billStatus()).isEqualTo(RepayBillStatus.NORMAL);
        assertThat(result.bills().getFirst().nextDueAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.bills().getFirst().applyAmtDisplay()).isEqualTo("Rp 1.500.000");
        assertThat(result.bills().getFirst().canReloan()).isFalse();
    }

    @Test
    void syncsSettledBillsFromLender() {
        when(lenderLoanBillListPort.listBills("partner-1", List.of("SETTLE"))).thenReturn(
                new LenderLoanBillListPort.LenderLoanBillListResult(
                        99L,
                        List.of(bill("LOAN-2", RepayBillStatus.SETTLE))
                )
        );

        LoanBillsFacade.BillsResult result = facade.listBills(1L, "partner-1", "81234567890", "SETTLED");

        assertThat(result.bills()).hasSize(1);
        assertThat(result.bills().getFirst().billStatus()).isEqualTo(RepayBillStatus.SETTLE);
        assertThat(result.bills().getFirst().canReloan()).isTrue();
    }

    private static LenderLoanBillListPort.LenderLoanBill bill(String loanApplyId, String billStatus) {
        return new LenderLoanBillListPort.LenderLoanBill(
                loanApplyId,
                "LN-" + loanApplyId,
                "USR-1",
                "BN-" + loanApplyId,
                new BigDecimal("1500000"),
                billStatus,
                1749792000000L,
                new BigDecimal("295000")
        );
    }
}
