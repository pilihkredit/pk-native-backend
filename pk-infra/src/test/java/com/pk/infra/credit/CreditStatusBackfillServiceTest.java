package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditStatusBackfillServiceTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditStatusPollHandler creditStatusPollHandler;

    private CreditStatusBackfillProperties properties;
    private CreditStatusBackfillService service;

    @BeforeEach
    void setUp() {
        properties = new CreditStatusBackfillProperties();
        properties.setBatchSize(2);
        properties.setLookbackDays(1);
        properties.setZoneId("Asia/Jakarta");
        service = new CreditStatusBackfillService(
                creditApplicationRepository,
                creditStatusPollHandler,
                properties
        );
    }

    @Test
    void drainsPagesUsingConfiguredLookbackWindow() {
        CreditApplicationRepository.CreditApplicationRecord first = record(1L, "A1");
        CreditApplicationRepository.CreditApplicationRecord second = record(2L, "A2");
        CreditApplicationRepository.CreditApplicationRecord third = record(3L, "A3");
        Instant expectedFrom = LocalDate.now(ZoneId.of("Asia/Jakarta"))
                .atStartOfDay(ZoneId.of("Asia/Jakarta"))
                .toInstant();
        when(creditApplicationRepository.findDueForStatusBackfill(eq(0L), eq(2), any()))
                .thenReturn(List.of(first, second));
        when(creditApplicationRepository.findDueForStatusBackfill(eq(2L), eq(2), any()))
                .thenReturn(List.of(third));
        doAnswer(invocation -> {
            CreditApplicationRepository.CreditApplicationRecord record = invocation.getArgument(0);
            if ("A2".equals(record.applyId())) {
                throw new RuntimeException("boom");
            }
            return null;
        }).when(creditStatusPollHandler).syncFromLenderForJob(any());

        CreditStatusBackfillService.BackfillResult result = service.run();

        assertThat(result.candidates()).isEqualTo(3);
        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.pages()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.createdFromInclusive()).isEqualTo(expectedFrom);
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(creditApplicationRepository, times(2))
                .findDueForStatusBackfill(anyLong(), anyInt(), fromCaptor.capture());
        assertThat(fromCaptor.getAllValues()).containsOnly(expectedFrom);
        verify(creditStatusPollHandler, times(3)).syncFromLenderForJob(any());
    }

    @Test
    void lookbackDaysNonPositiveMeansAllHistory() {
        properties.setLookbackDays(0);
        when(creditApplicationRepository.findDueForStatusBackfill(0L, 2, null))
                .thenReturn(List.of());

        CreditStatusBackfillService.BackfillResult result = service.run();

        assertThat(result.candidates()).isZero();
        assertThat(result.createdFromInclusive()).isNull();
        verify(creditApplicationRepository).findDueForStatusBackfill(eq(0L), eq(2), isNull());
    }

    private static CreditApplicationRepository.CreditApplicationRecord record(long id, String applyId) {
        return new CreditApplicationRepository.CreditApplicationRecord(
                id,
                applyId,
                "req-" + id,
                "apipartner",
                id,
                "partner-" + id,
                "CA-" + id
        );
    }
}
