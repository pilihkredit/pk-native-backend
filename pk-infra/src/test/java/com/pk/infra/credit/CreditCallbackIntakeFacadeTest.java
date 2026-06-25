package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.CreditProviderCode;
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
class CreditCallbackIntakeFacadeTest {
    @Mock
    private CallbackEventRepository callbackEventRepository;
    @Mock
    private CreditCallbackOutboxPublisher creditCallbackOutboxPublisher;
    @Mock
    private CreditCallbackParser callbackParser;

    private CreditCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CreditCallbackIntakeFacade(
                callbackEventRepository,
                creditCallbackOutboxPublisher,
                callbackParser
        );
    }

    @Test
    void publishesOutboxForNewCallback() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(callbackEventRepository.insert(any())).thenReturn(42L);

        CreditCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(42L);
        assertThat(result.duplicate()).isFalse();
        verify(creditCallbackOutboxPublisher).publish(42L, "AP-001");
    }

    @Test
    void skipsOutboxForDuplicateCallback() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existingRecord()));

        CreditCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.callbackEventId()).isEqualTo(7L);
        assertThat(result.duplicate()).isTrue();
        verify(creditCallbackOutboxPublisher, never()).publish(any(Long.class), any());
    }

    @Test
    void buildsExpectedIdempotencyKey() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(callbackEventRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(callbackEventRepository.insert(any())).thenReturn(1L);

        facade.intake("{}");

        ArgumentCaptor<CallbackEventRepository.CallbackEventInsert> captor =
                ArgumentCaptor.forClass(CallbackEventRepository.CallbackEventInsert.class);
        verify(callbackEventRepository).insert(captor.capture());
        assertThat(captor.getValue().idempotencyKey())
                .isEqualTo("pendanaan:CREDIT_RESULT:AP-001:SUCCESS:CA-001");
        assertThat(captor.getValue().callbackType()).isEqualTo(CallbackTypes.CREDIT_RESULT);
        assertThat(captor.getValue().providerCode()).isEqualTo(CreditProviderCode.PENDANAAN);
        assertThat(captor.getValue().processStatus()).isEqualTo(CallbackProcessStatus.RECEIVED);
    }

    private static CreditCallbackParser.ParsedCreditCallback parsedCallback() {
        return new CreditCallbackParser.ParsedCreditCallback(
                "AP-001",
                "CA-001",
                "SUCCESS",
                1893456000000L,
                null,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ONE
        );
    }

    private static CallbackEventRepository.CallbackEventRecord existingRecord() {
        return new CallbackEventRepository.CallbackEventRecord(
                7L,
                "CB-1",
                CreditProviderCode.PENDANAAN,
                CallbackTypes.CREDIT_RESULT,
                "AP-001",
                "key",
                "SUCCESS",
                "{}",
                CallbackProcessStatus.PROCESSED
        );
    }
}
