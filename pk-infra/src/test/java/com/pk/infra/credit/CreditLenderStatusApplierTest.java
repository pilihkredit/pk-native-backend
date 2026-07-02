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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditLenderStatusApplierTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditStatusHistoryRepository creditStatusHistoryRepository;
    @Mock
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;

    private CreditApplyProperties creditApplyProperties;
    private CreditLenderStatusApplier applier;

    @BeforeEach
    void setUp() {
        creditApplyProperties = new CreditApplyProperties();
        creditApplyProperties.setPollIntervalSeconds(30);
        applier = new CreditLenderStatusApplier(
                creditApplicationRepository,
                creditStatusHistoryRepository,
                creditLenderStatusQueryRepository,
                creditApplyProperties
        );
    }

    @Test
    void skipsCreditStatusUpdateWhenLenderStatusIsBlank() {
        CreditApplicationRepository.CreditApplicationRecord record = processingRecord();

        applier.apply(record, lenderStatus(""), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditApplicationRepository, never()).updateStatus(any(Long.class), any(), any(), any());
        verify(creditStatusHistoryRepository, never()).insert(any(Long.class), any(), any(), any(), any(), any());
        verify(creditApplicationRepository, never()).markSubmitted(any(Long.class), any(), any());
        verify(creditApplicationRepository).scheduleNextPoll(eq(1L), any());
    }

    @Test
    void updatesCreditStatusWhenLenderStatusIsPresent() {
        CreditApplicationRepository.CreditApplicationRecord record = processingRecord();

        applier.apply(record, lenderStatus("SUCCESS"), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository).upsert(any());
        verify(creditApplicationRepository).updateStatus(
                1L,
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                null
        );
        verify(creditStatusHistoryRepository).insert(
                1L,
                record.mobileNo(),
                CreditApplicationStatus.PROCESSING,
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                "CREDIT_STATUS_API"
        );
        verify(creditApplicationRepository, never()).scheduleNextPoll(any(Long.class), any());
    }

    @Test
    void skipsWhenApplicationIsAlreadyTerminal() {
        CreditApplicationRepository.CreditApplicationRecord record = new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                9L,
                "CA-1",
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                null
        );

        applier.apply(record, lenderStatus("REFUSED"), "CREDIT_STATUS_API", "LENDER_API");

        verify(creditLenderStatusQueryRepository, never()).upsert(any());
        verify(creditApplicationRepository, never()).updateStatus(any(Long.class), any(), any(), any());
    }

    private static CreditApplicationRepository.CreditApplicationRecord processingRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                9L,
                "CA-1",
                CreditApplicationStatus.PROCESSING,
                "PROCESSING",
                null
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
