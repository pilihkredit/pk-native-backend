package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.credit.CreditProviderCode;
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
    private CallbackEventRepository callbackEventRepository;
    @Mock
    private LoanCallbackOutboxPublisher loanCallbackOutboxPublisher;
    @Mock
    private LoanCallbackParser loanCallbackParser;

    private LoanCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanCallbackIntakeFacade(
                callbackEventRepository,
                loanCallbackOutboxPublisher,
                loanCallbackParser
        );
    }

    @Test
    void publishesOutboxForNewCallback() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(callbackEventRepository.insert(any())).thenReturn(42L);

        LoanCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(42L);
        assertThat(result.duplicate()).isFalse();
        verify(loanCallbackOutboxPublisher).publish(42L, "LOAN-001");
    }

    @Test
    void skipsOutboxForDuplicateCallback() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existingRecord()));

        LoanCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(7L);
        assertThat(result.duplicate()).isTrue();
        verify(loanCallbackOutboxPublisher, never()).publish(any(Long.class), any());
    }

    @Test
    void buildsExpectedIdempotencyKey() {
        when(loanCallbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(callbackEventRepository.insert(any())).thenReturn(1L);

        facade.intake("{}");

        ArgumentCaptor<CallbackEventRepository.CallbackEventInsert> captor =
                ArgumentCaptor.forClass(CallbackEventRepository.CallbackEventInsert.class);
        verify(callbackEventRepository).insert(captor.capture());
        assertThat(captor.getValue().idempotencyKey())
                .isEqualTo("pendanaan:LOAN_RESULT:LOAN-001:SUCCESS:LN-001");
        assertThat(captor.getValue().callbackType()).isEqualTo(CallbackTypes.LOAN_RESULT);
        assertThat(captor.getValue().providerCode()).isEqualTo(CreditProviderCode.PENDANAAN);
        assertThat(captor.getValue().processStatus()).isEqualTo(CallbackProcessStatus.RECEIVED);
    }

    private static LoanCallbackParser.ParsedLoanCallback parsedCallback() {
        return new LoanCallbackParser.ParsedLoanCallback(
                "LOAN-001",
                "LN-001",
                "SUCCESS",
                "BN-001",
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                1749792000000L
        );
    }

    private static CallbackEventRepository.CallbackEventRecord existingRecord() {
        return new CallbackEventRepository.CallbackEventRecord(
                7L,
                "CB-1",
                CreditProviderCode.PENDANAAN,
                CallbackTypes.LOAN_RESULT,
                "LOAN-001",
                "key",
                "SUCCESS",
                "{}",
                CallbackProcessStatus.PROCESSED
        );
    }
}
