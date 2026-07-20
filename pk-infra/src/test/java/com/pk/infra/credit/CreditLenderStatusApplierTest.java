package com.pk.infra.credit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditLenderStatusApplierTest {
    @Mock
    private CreditStatusHistoryRepository creditStatusHistoryRepository;
    @Mock
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;

    private CreditLenderStatusApplier applier;

    @BeforeEach
    void setUp() {
        applier = new CreditLenderStatusApplier(
                creditStatusHistoryRepository,
                creditLenderStatusQueryRepository
        );
    }

    @Test
    void upsertsWithoutHistoryWhenLenderStatusIsBlank() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findByApplyId("APPLY-1")).thenReturn(Optional.empty());

        applier.apply(record, lenderStatus(""), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditStatusHistoryRepository, never()).insert(any(Long.class), any(), any(), any(), any(), any());
    }

    @Test
    void writesHistoryOnFirstSuccess() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findByApplyId("APPLY-1")).thenReturn(Optional.empty());

        applier.apply(record, lenderStatus("SUCCESS"), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditStatusHistoryRepository).insert(
                1L,
                record.mobileNo(),
                null,
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                "CREDIT_STATUS_API"
        );
    }

    @Test
    void skipsHistoryWhenMappedStatusUnchanged() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findByApplyId("APPLY-1")).thenReturn(Optional.of(existingQuery("SUCCESS")));

        applier.apply(record, lenderStatus("SUCCESS"), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditStatusHistoryRepository, never()).insert(any(Long.class), any(), any(), any(), any(), any());
    }

    @Test
    void writesHistoryWhenMappedStatusChanges() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findByApplyId("APPLY-1")).thenReturn(Optional.of(existingQuery("SUCCESS")));

        applier.apply(record, lenderStatus("REFUSED"), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditStatusHistoryRepository).insert(
                1L,
                record.mobileNo(),
                CreditApplicationStatus.APPROVED,
                CreditApplicationStatus.REJECTED,
                "REFUSED",
                "CREDIT_STATUS_API"
        );
    }

    private static CreditApplicationRepository.CreditApplicationRecord applicationRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                "CA-1"
        );
    }

    private static CreditLenderStatusQueryRepository.CreditLenderStatusQueryData existingQuery(String externalStatus) {
        return new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                "APPLY-1",
                1L,
                "81234567890",
                "partner-1",
                "lender-user-1",
                "CA-1",
                externalStatus,
                1893456000000L,
                null,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ONE,
                "{}",
                "{}",
                Instant.now()
        );
    }

    private static LenderCreditPort.LenderCreditStatusResult lenderStatus(String externalStatus) {
        return new LenderCreditPort.LenderCreditStatusResult(
                externalStatus,
                "lender-user-1",
                "CA-1",
                1893456000000L,
                null,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ONE,
                "{}",
                "{\"status\":\"" + externalStatus + "\"}"
        );
    }
}
