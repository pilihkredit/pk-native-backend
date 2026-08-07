package com.pk.infra.loan;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanLenderStatusApplierTest {
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanStatusHistoryRepository loanStatusHistoryRepository;
    @Mock
    private LoanLenderStatusQueryRepository loanLenderStatusQueryRepository;

    private LoanApplyProperties loanApplyProperties;
    private LoanLenderStatusApplier applier;

    @BeforeEach
    void setUp() {
        loanApplyProperties = new LoanApplyProperties();
        loanApplyProperties.setPollIntervalSeconds(30);
        applier = new LoanLenderStatusApplier(
                loanApplicationRepository,
                loanStatusHistoryRepository,
                loanLenderStatusQueryRepository,
                loanApplyProperties
        );
    }

    @Test
    void insertsWhenNoPreviousSnapshot() {
        LoanApplicationRepository.LoanApplicationRecord record = processingRecord();
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyId("LOAN-1")).thenReturn(Optional.empty());

        applier.apply(record, lenderStatus("PROCESSING"), "APP");

        ArgumentCaptor<LoanLenderStatusQueryRepository.LoanLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(LoanLenderStatusQueryRepository.LoanLenderStatusQueryData.class);
        verify(loanLenderStatusQueryRepository).insert(captor.capture());
        LoanLenderStatusQueryRepository.LoanLenderStatusQueryData saved = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(saved.loanApplyId()).isEqualTo("LOAN-1");
        org.assertj.core.api.Assertions.assertThat(saved.externalStatus()).isEqualTo("PROCESSING");
        org.assertj.core.api.Assertions.assertThat(saved.externalInteractionId()).isEqualTo(99L);
        org.assertj.core.api.Assertions.assertThat(saved.externalInteractionCallbackId()).isNull();
    }

    @Test
    void skipsInsertWhenBusinessFieldsUnchanged() {
        LoanApplicationRepository.LoanApplicationRecord record = processingRecord();
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyId("LOAN-1"))
                .thenReturn(Optional.of(existingQuery("PROCESSING")));

        applier.apply(record, lenderStatus("PROCESSING"), "APP");

        verify(loanLenderStatusQueryRepository, never()).insert(any());
    }

    @Test
    void insertsWhenExternalStatusChanges() {
        LoanApplicationRepository.LoanApplicationRecord record = processingRecord();
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyId("LOAN-1"))
                .thenReturn(Optional.of(existingQuery("PROCESSING")));

        applier.apply(record, lenderStatus("SUCCESS"), "APP");

        ArgumentCaptor<LoanLenderStatusQueryRepository.LoanLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(LoanLenderStatusQueryRepository.LoanLenderStatusQueryData.class);
        verify(loanLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalStatus()).isEqualTo("SUCCESS");
        verify(loanApplicationRepository).updateStatus(10L, LoanApplicationStatus.DISBURSED, "SUCCESS");
    }

    @Test
    void insertsCallbackIdWhenProvided() {
        LoanApplicationRepository.LoanApplicationRecord record = processingRecord();
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyId("LOAN-1")).thenReturn(Optional.empty());

        applier.apply(record, callbackStatus("SUCCESS"), "CALLBACK", 77L);

        ArgumentCaptor<LoanLenderStatusQueryRepository.LoanLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(LoanLenderStatusQueryRepository.LoanLenderStatusQueryData.class);
        verify(loanLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionCallbackId()).isEqualTo(77L);
        verify(loanApplicationRepository).updateStatus(10L, LoanApplicationStatus.DISBURSED, "SUCCESS");
    }

    @Test
    void applyMainRecordSkipsStatusQuerySnapshot() {
        LoanApplicationRepository.LoanApplicationRecord record = processingRecord();

        applier.applyMainRecord(record, callbackStatus("SUCCESS"), "CALLBACK");

        verify(loanLenderStatusQueryRepository, never()).insert(any());
        verify(loanLenderStatusQueryRepository, never()).findLatestByLoanApplyId(any());
        verify(loanApplicationRepository).updateStatus(10L, LoanApplicationStatus.DISBURSED, "SUCCESS");
    }

    private static LoanApplicationRepository.LoanApplicationRecord processingRecord() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                10L,
                "LOAN-1",
                "REQ-1",
                "APPLY-1",
                100L,
                200L,
                "QUOTE-1",
                1L,
                300L,
                "LN-OLD",
                "USR-1",
                null,
                LoanApplicationStatus.PROCESSING,
                "PROCESSING",
                new BigDecimal("1500000"),
                null,
                null
        );
    }

    private static LoanLenderStatusQueryRepository.LoanLenderStatusQueryData existingQuery(String externalStatus) {
        return new LoanLenderStatusQueryRepository.LoanLenderStatusQueryData(
                "LOAN-1",
                1L,
                "USR-1",
                "LN-1",
                externalStatus,
                null,
                new BigDecimal("1500000"),
                null,
                null,
                null,
                99L,
                null,
                "APP",
                Instant.now()
        );
    }

    private static LenderLoanStatusPort.LenderLoanStatusResult lenderStatus(String externalStatus) {
        return new LenderLoanStatusPort.LenderLoanStatusResult(
                externalStatus,
                "LN-1",
                null,
                new BigDecimal("1500000"),
                null,
                null,
                null,
                99L
        );
    }

    private static LenderLoanStatusPort.LenderLoanStatusResult callbackStatus(String externalStatus) {
        return new LenderLoanStatusPort.LenderLoanStatusResult(
                externalStatus,
                "LN-1",
                "BILL-1",
                new BigDecimal("1500000"),
                new BigDecimal("1450000"),
                1782864000000L,
                1749200000000L,
                null
        );
    }
}
