package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditStatusBackfillServiceTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditStatusPollHandler creditStatusPollHandler;

    private CreditStatusBackfillService service;

    @BeforeEach
    void setUp() {
        service = new CreditStatusBackfillService(creditApplicationRepository, creditStatusPollHandler);
    }

    @Test
    void runsBatchAndCountsFailures() {
        CreditApplicationRepository.CreditApplicationRecord ok = record(1L, "A1");
        CreditApplicationRepository.CreditApplicationRecord bad = record(2L, "A2");
        when(creditApplicationRepository.findDueForStatusBackfill(50)).thenReturn(List.of(ok, bad));
        doAnswer(invocation -> {
            CreditApplicationRepository.CreditApplicationRecord record = invocation.getArgument(0);
            if ("A2".equals(record.applyId())) {
                throw new RuntimeException("boom");
            }
            return null;
        }).when(creditStatusPollHandler).syncFromLenderForJob(any());

        CreditStatusBackfillService.BackfillResult result = service.run();

        assertThat(result.candidates()).isEqualTo(2);
        assertThat(result.success()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        verify(creditStatusPollHandler, times(2)).syncFromLenderForJob(any());
    }

    private static CreditApplicationRepository.CreditApplicationRecord record(long id, String applyId) {
        return new CreditApplicationRepository.CreditApplicationRecord(
                id,
                applyId,
                "req-" + id,
                "pendanaan",
                id,
                "partner-" + id,
                "CA-" + id
        );
    }
}
