package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepayTrialBackfillServiceTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private RepaymentPlanTermRepository repaymentPlanTermRepository;
    @Mock
    private RepayTrialFacade repayTrialFacade;
    @Mock
    private UserAuthRepository userAuthRepository;

    private RepayTrialBackfillProperties properties;
    private RepayTrialBackfillService service;

    @BeforeEach
    void setUp() {
        properties = new RepayTrialBackfillProperties();
        properties.setBatchSize(2);
        properties.setLookbackDays(1);
        properties.setZoneId("Asia/Jakarta");
        service = new RepayTrialBackfillService(
                loanBillReadRepository,
                repaymentPlanTermRepository,
                repayTrialFacade,
                userAuthRepository,
                properties
        );
    }

    @Test
    void drainsPagesUsingPendingTerms() {
        Instant expectedFrom = LocalDate.now(ZoneId.of("Asia/Jakarta"))
                .atStartOfDay(ZoneId.of("Asia/Jakarta"))
                .toInstant();
        when(loanBillReadRepository.findDueForTrialBackfill(0L, 2, expectedFrom))
                .thenReturn(List.of(candidate(1L, "L1"), candidate(2L, "L2")));
        when(loanBillReadRepository.findDueForTrialBackfill(2L, 2, expectedFrom))
                .thenReturn(List.of(candidate(3L, "L3")));
        when(repaymentPlanTermRepository.findByLoanApplicationId(anyLong()))
                .thenReturn(List.of(term(1, "UNPAID"), term(2, "OVERDUE")));
        when(userAuthRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            RepayTrialFacade.TrialCommand command = invocation.getArgument(2);
            if ("L2".equals(command.loanApplyId())) {
                throw new RuntimeException("boom");
            }
            return new RepayTrialFacade.TrialResult("RT-1", Instant.now().toEpochMilli(), null);
        }).when(repayTrialFacade).syncFromLenderForJob(anyLong(), any(), any());

        RepayTrialBackfillService.BackfillResult result = service.run();

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.pages()).isEqualTo(2);
        verify(repayTrialFacade, times(3)).syncFromLenderForJob(anyLong(), any(), any());
    }

    @Test
    void lookbackDaysNonPositiveMeansAllHistory() {
        properties.setLookbackDays(0);
        when(loanBillReadRepository.findDueForTrialBackfill(0L, 2, null)).thenReturn(List.of());

        RepayTrialBackfillService.BackfillResult result = service.run();

        assertThat(result.createdFromInclusive()).isNull();
        verify(loanBillReadRepository).findDueForTrialBackfill(eq(0L), eq(2), isNull());
    }

    private static LoanBillReadRepository.TrialBackfillCandidate candidate(long id, String loanApplyId) {
        return new LoanBillReadRepository.TrialBackfillCandidate(id, id, loanApplyId);
    }

    private static RepaymentPlanTermRepository.TermRecord term(int termNo, String status) {
        return new RepaymentPlanTermRepository.TermRecord(
                termNo,
                1L,
                "LOAN",
                "BILL",
                "SUB",
                termNo,
                status,
                Instant.now(),
                Instant.now(),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                0,
                null,
                null,
                Instant.now()
        );
    }
}
