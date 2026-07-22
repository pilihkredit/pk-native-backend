package com.pk.infra.loan;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanCallbackHandlerTest {
    @Mock
    private CallbackEventRepository callbackEventRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanCallbackParser loanCallbackParser;
    @Mock
    private LoanLenderStatusApplier loanLenderStatusApplier;

    private LoanCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoanCallbackHandler(
                callbackEventRepository,
                loanApplicationRepository,
                loanCallbackParser,
                loanLenderStatusApplier
        );
    }

    @Test
    void ignoresUnknownLoanApplyId() {
        when(callbackEventRepository.findById(1L)).thenReturn(Optional.of(callbackEvent()));
        when(loanApplicationRepository.findByLoanApplyId("LOAN-001")).thenReturn(Optional.empty());

        handler.handle(new LoanCallbackJob(1L, "LOAN-001"));

        verify(callbackEventRepository).markIgnored(eq(1L), any(Instant.class));
        verify(loanLenderStatusApplier, never()).apply(any(), any(), any());
    }

    @Test
    void appliesStatusForKnownLoanApplyId() {
        LoanApplicationRepository.LoanApplicationRecord record = applicationRecord();
        when(callbackEventRepository.findById(1L)).thenReturn(Optional.of(callbackEvent()));
        when(loanApplicationRepository.findByLoanApplyId("LOAN-001")).thenReturn(Optional.of(record));
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());

        handler.handle(new LoanCallbackJob(1L, "LOAN-001"));

        verify(loanLenderStatusApplier).apply(
                eq(record),
                any(LenderLoanStatusPort.LenderLoanStatusResult.class),
                eq("LOAN_CALLBACK")
        );
        verify(callbackEventRepository).markProcessed(eq(1L), any(Instant.class));
    }

    private static CallbackEventRepository.CallbackEventRecord callbackEvent() {
        return new CallbackEventRepository.CallbackEventRecord(
                1L,
                "CB-1",
                CreditProviderCode.PENDANAAN,
                CallbackTypes.LOAN_RESULT,
                "LOAN-001",
                "key",
                "SUCCESS",
                "{}",
                CallbackProcessStatus.RECEIVED
        );
    }

    private static LoanApplicationRepository.LoanApplicationRecord applicationRecord() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                10L,
                "LOAN-001",
                "REQ-001",
                "APPLY-001",
                "81234567890",
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
                null,
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
