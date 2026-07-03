package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.infra.loan.LoanProductFacade;
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
class CreditLimitDisplayFacadeTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    @Mock
    private LoanProductFacade loanProductFacade;

    private CreditLimitDisplayFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CreditLimitDisplayFacade(
                creditApplicationRepository,
                creditLenderStatusQueryRepository,
                loanProductFacade
        );
    }

    @Test
    void usesFakeLimitAsDefaultForUniformRepayMethod() {
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLenderStatusQueryRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(statusQuery()));
        when(loanProductFacade.findRepayMethod(1L, "APPLY-1", "RP001"))
                .thenReturn(new LenderRepayMethod("RP001", "D", 30, 6, 180, 0, null, List.of()));

        CreditLimitDisplayFacade.LimitDisplayResult result = facade.getLimitDisplay(1L, "APPLY-1", "RP001");

        assertThat(result.repaymentUniform()).isTrue();
        assertThat(result.defaultAmount()).isEqualByComparingTo("2800000");
    }

    @Test
    void usesMaxLimitAsDefaultForUnevenRepayMethod() {
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLenderStatusQueryRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(statusQuery()));
        when(loanProductFacade.findRepayMethod(1L, "APPLY-1", "RP002"))
                .thenReturn(new LenderRepayMethod(
                        "RP002",
                        "D",
                        30,
                        2,
                        60,
                        1,
                        null,
                        List.of(new LenderRepayMethod.UnevenBillRate(1, new BigDecimal("0.6")))
                ));

        CreditLimitDisplayFacade.LimitDisplayResult result = facade.getLimitDisplay(1L, "APPLY-1", "RP002");

        assertThat(result.repaymentUniform()).isFalse();
        assertThat(result.defaultAmount()).isEqualByComparingTo("3000000");
    }

    @Test
    void rejectsWhenLimitSnapshotMissing() {
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLenderStatusQueryRepository.findByApplyIdAndProfileId("APPLY-1", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.getLimitDisplay(1L, "APPLY-1", null))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE);
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
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
    }

    private static CreditLenderStatusQueryRepository.CreditLenderStatusQueryData statusQuery() {
        return new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                "APPLY-1",
                1L,
                "81234567890",
                "partner-1",
                "USR-1",
                "CA-1",
                "SUCCESS",
                Instant.parse("2026-06-30T00:00:00Z").toEpochMilli(),
                null,
                new BigDecimal("500000"),
                new BigDecimal("3000000"),
                new BigDecimal("2500000"),
                new BigDecimal("2800000"),
                new BigDecimal("100000"),
                "{}",
                "{}",
                Instant.parse("2026-06-01T00:00:00Z")
        );
    }
}
