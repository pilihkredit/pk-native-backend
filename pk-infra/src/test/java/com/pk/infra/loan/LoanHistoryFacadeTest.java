package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.port.LenderLoanHistoryPort;
import com.pk.core.loan.port.LoanLenderHistoryOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanHistoryFacadeTest {
    @Mock
    private LenderLoanHistoryPort lenderLoanHistoryPort;
    @Mock
    private LoanLenderHistoryOrderRepository loanLenderHistoryOrderRepository;

    private LoanHistoryFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanHistoryFacade(lenderLoanHistoryPort, loanLenderHistoryOrderRepository);
    }

    @Test
    void upsertsEachLenderOrderByLoanApplyIdAndReturnsList() {
        when(lenderLoanHistoryPort.queryHistory("partner-1")).thenReturn(
                new LenderLoanHistoryPort.LenderLoanHistoryResult(
                        99L,
                        List.of(
                                order("LOAN-1", "SUCCESS"),
                                order("LOAN-2", "REFUSED")
                        )
                )
        );

        LoanHistoryFacade.HistoryResult result = facade.listHistory(1L, "partner-1", "81234567890");

        ArgumentCaptor<LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData> captor =
                ArgumentCaptor.forClass(LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData.class);
        verify(loanLenderHistoryOrderRepository, org.mockito.Mockito.times(2)).upsert(captor.capture());

        List<LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData> saved = captor.getAllValues();
        assertThat(saved).extracting(LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData::loanApplyId)
                .containsExactly("LOAN-1", "LOAN-2");
        LoanLenderHistoryOrderRepository.LoanLenderHistoryOrderData first = saved.getFirst();
        assertThat(first.profileId()).isEqualTo(1L);
        assertThat(first.mobileNo()).isEqualTo("81234567890");
        assertThat(first.externalLoanApplyNo()).isEqualTo("LN-LOAN-1");
        assertThat(first.lenderUserId()).isEqualTo("USR-1");
        assertThat(first.externalStatus()).isEqualTo("SUCCESS");
        assertThat(first.externalInteractionId()).isEqualTo(99L);
        assertThat(first.queriedAt()).isNotNull();

        assertThat(result.orders()).hasSize(2);
        assertThat(result.orders().get(0).loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.orders().get(1).externalStatus()).isEqualTo("REFUSED");
    }

    private static LenderLoanHistoryPort.LenderLoanHistoryOrder order(String loanApplyId, String status) {
        return new LenderLoanHistoryPort.LenderLoanHistoryOrder(
                loanApplyId,
                "LN-" + loanApplyId,
                "USR-1",
                status,
                "SUCCESS".equals(status) ? "BILL-" + loanApplyId : null,
                new BigDecimal("1000000.00"),
                "SUCCESS".equals(status) ? new BigDecimal("970000.00") : null,
                "SUCCESS".equals(status) ? 1749792000000L : null,
                "REFUSED".equals(status) ? 1749200000000L : null,
                1749791000000L
        );
    }
}
