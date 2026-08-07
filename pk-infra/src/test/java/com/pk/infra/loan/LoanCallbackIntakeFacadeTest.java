package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.external.ExternalInteractionCallbackLog;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanCallbackIntakeFacadeTest {
    @Mock
    private ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository;
    @Mock
    private LoanCallbackParser loanCallbackParser;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanLenderStatusApplier loanLenderStatusApplier;
    @Mock
    private UserAuthRepository userAuthRepository;

    private LoanCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanCallbackIntakeFacade(
                externalInteractionCallbackLogRepository,
                loanCallbackParser,
                loanApplicationRepository,
                loanLenderStatusApplier,
                userAuthRepository
        );
    }

    @Test
    void processesCallbackSynchronouslyForKnownLoanApplyId() {
        LoanApplicationRepository.LoanApplicationRecord record = applicationRecord();
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(99L);
            return 99L;
        });
        when(loanApplicationRepository.findByLoanApplyId("LOAN-001")).thenReturn(Optional.of(record));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "partner-1", "81234567890", false)
        ));

        LoanCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.externalInteractionCallbackId()).isEqualTo(99L);
        assertThat(result.duplicate()).isFalse();
        assertThat(result.ignored()).isFalse();
        verify(loanLenderStatusApplier).apply(
                eq(record),
                any(LenderLoanStatusPort.LenderLoanStatusResult.class),
                eq("CALLBACK"),
                eq(99L)
        );
        verify(externalInteractionCallbackLogRepository).updateResponse(
                eq(99L),
                eq("81234567890"),
                eq(1L),
                eq("000000"),
                eq("success"),
                eq("{\"code\":\"000000\",\"msg\":\"success\"}"),
                eq(true),
                any(Integer.class)
        );
    }

    @Test
    void skipsBusinessUpdateForDuplicateCallback() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.of(7L));

        LoanCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.externalInteractionCallbackId()).isEqualTo(7L);
        assertThat(result.duplicate()).isTrue();
        verify(externalInteractionCallbackLogRepository, never()).insert(any());
        verify(loanLenderStatusApplier, never()).apply(any(), any(), any(), any());
    }

    @Test
    void ignoresUnknownLoanApplyIdAfterAuditInsert() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(55L);
            return 55L;
        });
        when(loanApplicationRepository.findByLoanApplyId("LOAN-001")).thenReturn(Optional.empty());

        LoanCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.ignored()).isTrue();
        verify(loanLenderStatusApplier, never()).apply(any(), any(), any(), any());
        verify(externalInteractionCallbackLogRepository).updateResponse(
                eq(55L),
                isNull(),
                isNull(),
                eq("000000"),
                eq("success"),
                eq("{\"code\":\"000000\",\"msg\":\"success\"}"),
                eq(true),
                any(Integer.class)
        );
    }

    @Test
    void buildsExpectedIdempotencyKeyOnInsert() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(1L);
            return 1L;
        });
        when(loanApplicationRepository.findByLoanApplyId("LOAN-001")).thenReturn(Optional.empty());

        facade.intake("{}");

        ArgumentCaptor<ExternalInteractionCallbackLog> captor =
                ArgumentCaptor.forClass(ExternalInteractionCallbackLog.class);
        verify(externalInteractionCallbackLogRepository).insert(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey())
                .isEqualTo("pendanaan:LOAN_RESULT:LOAN-001:SUCCESS:LN-001");
        assertThat(captor.getValue().getBusinessType()).isEqualTo(CallbackTypes.LOAN_RESULT);
        assertThat(captor.getValue().getProviderCode()).isEqualTo(CreditProviderCode.PENDANAAN);
    }

    private static LoanApplicationRepository.LoanApplicationRecord applicationRecord() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                10L,
                "LOAN-001",
                "REQ-001",
                "APPLY-001",
                100L,
                200L,
                "QUOTE-001",
                1L,
                300L,
                null,
                null,
                null,
                LoanApplicationStatus.PROCESSING,
                null,
                new BigDecimal("1500000"),
                null,
                null
        );
    }

    private static LoanCallbackParser.ParsedLoanCallback parsedCallback() {
        return new LoanCallbackParser.ParsedLoanCallback(
                "LOAN-001",
                "LN-001",
                "SUCCESS",
                "BN-001",
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                1749792000000L,
                null
        );
    }
}
