package com.pk.infra.credit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.credit.port.CreditApplicationRepository;
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
class CreditCallbackHandlerTest {
    @Mock
    private CallbackEventRepository callbackEventRepository;
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditCallbackParser callbackParser;
    @Mock
    private CreditLenderStatusApplier creditLenderStatusApplier;

    private CreditCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreditCallbackHandler(
                callbackEventRepository,
                creditApplicationRepository,
                callbackParser,
                creditLenderStatusApplier
        );
    }

    @Test
    void ignoresUnknownApplyId() {
        when(callbackEventRepository.findById(1L)).thenReturn(Optional.of(callbackEvent()));
        when(creditApplicationRepository.findByApplyId("AP-001")).thenReturn(Optional.empty());

        handler.handle(new CreditCallbackJob(1L, "AP-001"));

        verify(callbackEventRepository).markIgnored(eq(1L), any(Instant.class));
        verify(creditLenderStatusApplier, never()).apply(any(), any(), any(), any());
    }

    @Test
    void appliesStatusForKnownApplyId() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(callbackEventRepository.findById(1L)).thenReturn(Optional.of(callbackEvent()));
        when(creditApplicationRepository.findByApplyId("AP-001")).thenReturn(Optional.of(record));
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());

        handler.handle(new CreditCallbackJob(1L, "AP-001"));

        verify(creditLenderStatusApplier).apply(
                eq(record),
                any(LenderCreditPort.LenderCreditStatusResult.class),
                eq("CREDIT_CALLBACK"),
                eq("LENDER_CALLBACK")
        );
        verify(creditApplicationRepository).updateLastLenderAudit(10L, null, "{}");
        verify(callbackEventRepository).markProcessed(eq(1L), any(Instant.class));
    }

    private static CallbackEventRepository.CallbackEventRecord callbackEvent() {
        return new CallbackEventRepository.CallbackEventRecord(
                1L,
                "CB-1",
                CreditProviderCode.PENDANAAN,
                CallbackTypes.CREDIT_RESULT,
                "AP-001",
                "key",
                "SUCCESS",
                "{}",
                CallbackProcessStatus.RECEIVED
        );
    }

    private static CreditApplicationRepository.CreditApplicationRecord applicationRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                10L,
                "AP-001",
                "req-1",
                CreditProviderCode.PENDANAAN,
                1L,
                "81234567890",
                2L,
                null,
                CreditApplicationStatus.PROCESSING,
                null,
                null
        );
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
}
