package com.pk.infra.credit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.DataWriteSource;
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
class CreditLenderStatusApplierTest {
    @Mock
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;

    private CreditLenderStatusApplier applier;

    @BeforeEach
    void setUp() {
        applier = new CreditLenderStatusApplier(creditLenderStatusQueryRepository);
    }

    @Test
    void insertsWhenNoPreviousSnapshot() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findLatestByApplyId("APPLY-1")).thenReturn(Optional.empty());

        applier.apply(record, lenderStatus("SUCCESS", 101L), DataWriteSource.APP);

        ArgumentCaptor<CreditLenderStatusQueryRepository.CreditLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData.class);
        verify(creditLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionId()).isEqualTo(101L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionCallbackId()).isNull();
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalStatus()).isEqualTo("SUCCESS");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().source()).isEqualTo(DataWriteSource.APP);
    }

    @Test
    void skipsInsertWhenBusinessFieldsUnchanged() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findLatestByApplyId("APPLY-1"))
                .thenReturn(Optional.of(existingQuery("SUCCESS")));

        applier.apply(record, lenderStatus("SUCCESS", 102L), DataWriteSource.APP);

        verify(creditLenderStatusQueryRepository, never()).insert(any());
    }

    @Test
    void insertsWhenExternalStatusChanges() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findLatestByApplyId("APPLY-1"))
                .thenReturn(Optional.of(existingQuery("SUCCESS")));

        applier.apply(record, lenderStatus("REFUSED", null), DataWriteSource.CALLBACK);

        ArgumentCaptor<CreditLenderStatusQueryRepository.CreditLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData.class);
        verify(creditLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalStatus()).isEqualTo("REFUSED");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().source()).isEqualTo(DataWriteSource.CALLBACK);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionId()).isNull();
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionCallbackId()).isNull();
    }

    @Test
    void insertsCallbackIdWhenProvided() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findLatestByApplyId("APPLY-1")).thenReturn(Optional.empty());

        applier.apply(record, lenderStatus("SUCCESS", null), DataWriteSource.CALLBACK, 77L);

        ArgumentCaptor<CreditLenderStatusQueryRepository.CreditLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData.class);
        verify(creditLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionCallbackId()).isEqualTo(77L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().externalInteractionId()).isNull();
    }

    @Test
    void insertsWhenLimitChangesEvenIfStatusSame() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(creditLenderStatusQueryRepository.findLatestByApplyId("APPLY-1"))
                .thenReturn(Optional.of(existingQuery("SUCCESS")));

        applier.apply(
                record,
                new LenderCreditPort.LenderCreditStatusResult(
                        "SUCCESS",
                        "lender-user-1",
                        "CA-1",
                        1893456000000L,
                        null,
                        BigDecimal.ONE,
                        new BigDecimal("20"),
                        BigDecimal.TEN,
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        55L
                ),
                DataWriteSource.JOB
        );

        ArgumentCaptor<CreditLenderStatusQueryRepository.CreditLenderStatusQueryData> captor =
                ArgumentCaptor.forClass(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData.class);
        verify(creditLenderStatusQueryRepository).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().source()).isEqualTo(DataWriteSource.JOB);
    }

    private static CreditApplicationRepository.CreditApplicationRecord applicationRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "CA-1"
        );
    }

    private static CreditLenderStatusQueryRepository.CreditLenderStatusQueryData existingQuery(String externalStatus) {
        return new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                "APPLY-1",
                1L,
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
                9L,
                null,
                DataWriteSource.APP,
                Instant.now()
        );
    }

    private static LenderCreditPort.LenderCreditStatusResult lenderStatus(String externalStatus, Long interactionId) {
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
                interactionId
        );
    }
}
