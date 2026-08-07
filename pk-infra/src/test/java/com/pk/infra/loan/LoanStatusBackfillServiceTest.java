package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.port.LoanApplicationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanStatusBackfillServiceTest {
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanStatusPollHandler loanStatusPollHandler;

    private LoanStatusBackfillProperties properties;
    private LoanStatusBackfillService service;

    @BeforeEach
    void setUp() {
        properties = new LoanStatusBackfillProperties();
        properties.setBatchSize(2);
        properties.setLookbackDays(1);
        properties.setZoneId("Asia/Jakarta");
        service = new LoanStatusBackfillService(loanApplicationRepository, loanStatusPollHandler, properties);
    }

    @Test
    void drainsPagesUsingConfiguredLookbackWindow() {
        Instant expectedFrom = LocalDate.now(ZoneId.of("Asia/Jakarta"))
                .atStartOfDay(ZoneId.of("Asia/Jakarta"))
                .toInstant();
        when(loanApplicationRepository.findDueForStatusBackfill(0L, 2, expectedFrom))
                .thenReturn(List.of(record(1L, "L1"), record(2L, "L2")));
        when(loanApplicationRepository.findDueForStatusBackfill(2L, 2, expectedFrom))
                .thenReturn(List.of(record(3L, "L3")));
        doAnswer(invocation -> {
            LoanApplicationRepository.LoanApplicationRecord record = invocation.getArgument(0);
            if ("L2".equals(record.loanApplyId())) {
                throw new RuntimeException("boom");
            }
            return null;
        }).when(loanStatusPollHandler).syncFromLenderForJob(any());

        LoanStatusBackfillService.BackfillResult result = service.run();

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.pages()).isEqualTo(2);
        assertThat(result.createdFromInclusive()).isEqualTo(expectedFrom);
        verify(loanStatusPollHandler, times(3)).syncFromLenderForJob(any());
    }

    @Test
    void lookbackDaysNonPositiveMeansAllHistory() {
        properties.setLookbackDays(0);
        when(loanApplicationRepository.findDueForStatusBackfill(0L, 2, null)).thenReturn(List.of());

        LoanStatusBackfillService.BackfillResult result = service.run();

        assertThat(result.createdFromInclusive()).isNull();
        verify(loanApplicationRepository).findDueForStatusBackfill(eq(0L), eq(2), isNull());
    }

    private static LoanApplicationRepository.LoanApplicationRecord record(long id, String loanApplyId) {
        return new LoanApplicationRepository.LoanApplicationRecord(
                id,
                loanApplyId,
                "req-" + id,
                "APPLY-1",
                1L,
                1L,
                "Q-1",
                id,
                1L,
                "LN-" + id,
                "USR-1",
                null,
                "PROCESSING",
                "PROCESSING",
                new BigDecimal("1000"),
                null,
                null
        );
    }
}
