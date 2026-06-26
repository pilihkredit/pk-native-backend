package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepayBillsOverviewFacadeTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private RepaymentPlanTermRepository repaymentPlanTermRepository;
    @Mock
    private RepayCurrentOrderRepository repayCurrentOrderRepository;

    private RepayBillsOverviewFacade facade;

    @BeforeEach
    void setUp() {
        facade = new RepayBillsOverviewFacade(
                loanBillReadRepository,
                repaymentPlanTermRepository,
                repayCurrentOrderRepository,
                new ObjectMapper(),
                Optional.empty()
        );
    }

    @Test
    void getOverviewAggregatesPendingBillsAndSelection() {
        when(loanBillReadRepository.findPendingByProfileId(1L)).thenReturn(List.of(loanRecord()));
        when(repaymentPlanTermRepository.findByLoanApplicationId(100L)).thenReturn(List.of(
                term(RepayTermStatus.OVERDUE, new BigDecimal("200000"), 3),
                term(RepayTermStatus.UNPAID, new BigDecimal("95000"), 0)
        ));
        when(repayCurrentOrderRepository.findActiveByProfileId(1L)).thenReturn(Optional.of(
                new RepayCurrentOrderRepository.CurrentOrderRecord(
                        1L,
                        1L,
                        "RCO-1",
                        10L,
                        "[{\"loanApplyId\":\"LOAN-1\",\"termNos\":[1]}]",
                        null,
                        "ACTIVE",
                        Instant.now()
                )
        ));

        RepayBillsOverviewFacade.OverviewResult result = facade.getOverview(1L);

        assertThat(result.totalBillCount()).isEqualTo(1);
        assertThat(result.totalDueAmount()).isEqualByComparingTo("295000");
        assertThat(result.totalDueAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.hasOverdue()).isTrue();
        assertThat(result.bills().getFirst().selected()).isTrue();
        assertThat(result.bills().getFirst().currentDueAmountDisplay()).isEqualTo("Rp 295.000");
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

    private static RepaymentPlanTermRepository.TermRecord term(
            String status,
            BigDecimal shouldAmount,
            int overdueDays
    ) {
        return new RepaymentPlanTermRepository.TermRecord(
                1L,
                100L,
                "LOAN-1",
                "BN-1",
                "SUB-1",
                1,
                status,
                Instant.now(),
                null,
                shouldAmount,
                shouldAmount,
                BigDecimal.ZERO,
                overdueDays,
                null,
                null,
                Instant.now()
        );
    }
}
