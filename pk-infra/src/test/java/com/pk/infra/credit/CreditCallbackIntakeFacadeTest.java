package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.ExternalInteractionCallbackLog;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import java.math.BigDecimal;
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
    private ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository;
    @Mock
    private CreditCallbackParser callbackParser;
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditLenderStatusApplier creditLenderStatusApplier;
    @Mock
    private UserAuthRepository userAuthRepository;

    private CreditCallbackIntakeFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CreditCallbackIntakeFacade(
                externalInteractionCallbackLogRepository,
                callbackParser,
                creditApplicationRepository,
                creditLenderStatusApplier,
                userAuthRepository
        );
    }

    @Test
    void processesCallbackSynchronouslyForKnownApplyId() {
        CreditApplicationRepository.CreditApplicationRecord record = applicationRecord();
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(99L);
            return 99L;
        });
        when(creditApplicationRepository.findByApplyId("AP-001")).thenReturn(Optional.of(record));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "partner-1", "81234567890", false)
        ));

        CreditCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.externalInteractionCallbackId()).isEqualTo(99L);
        assertThat(result.duplicate()).isFalse();
        assertThat(result.ignored()).isFalse();
        verify(creditLenderStatusApplier).apply(
                eq(record),
                any(LenderCreditPort.LenderCreditStatusResult.class),
                eq("CREDIT_CALLBACK"),
                eq("LENDER_CALLBACK"),
                eq(99L)
        );
        verify(externalInteractionCallbackLogRepository).updateResponse(
                eq(99L),
                eq("81234567890"),
                eq("000000"),
                eq("success"),
                eq("{\"code\":\"000000\",\"msg\":\"success\"}"),
                eq(true),
                any(Integer.class)
        );
    }

    @Test
    void skipsBusinessUpdateForDuplicateCallback() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.of(7L));

        CreditCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.externalInteractionCallbackId()).isEqualTo(7L);
        assertThat(result.duplicate()).isTrue();
        verify(externalInteractionCallbackLogRepository, never()).insert(any());
        verify(creditLenderStatusApplier, never()).apply(any(), any(), any(), any(), any());
    }

    @Test
    void ignoresUnknownApplyIdAfterAuditInsert() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(55L);
            return 55L;
        });
        when(creditApplicationRepository.findByApplyId("AP-001")).thenReturn(Optional.empty());

        CreditCallbackIntakeFacade.IntakeResult result = facade.intake("{}");

        assertThat(result.ignored()).isTrue();
        verify(creditLenderStatusApplier, never()).apply(any(), any(), any(), any(), any());
        verify(externalInteractionCallbackLogRepository).updateResponse(
                eq(55L),
                eq(null),
                eq("000000"),
                eq("success"),
                eq("{\"code\":\"000000\",\"msg\":\"success\"}"),
                eq(true),
                any(Integer.class)
        );
    }

    @Test
    void buildsExpectedIdempotencyKeyOnInsert() {
        when(callbackParser.parse("{}")).thenReturn(parsedCallback());
        when(externalInteractionCallbackLogRepository.findIdByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(externalInteractionCallbackLogRepository.insert(any(ExternalInteractionCallbackLog.class))).thenAnswer(invocation -> {
            ExternalInteractionCallbackLog log = invocation.getArgument(0);
            log.setId(1L);
            return 1L;
        });
        when(creditApplicationRepository.findByApplyId("AP-001")).thenReturn(Optional.empty());

        facade.intake("{}");

        ArgumentCaptor<ExternalInteractionCallbackLog> captor =
                ArgumentCaptor.forClass(ExternalInteractionCallbackLog.class);
        verify(externalInteractionCallbackLogRepository).insert(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey())
                .isEqualTo("pendanaan:CREDIT_RESULT:AP-001:SUCCESS:CA-001");
        assertThat(captor.getValue().getBusinessType()).isEqualTo(CallbackTypes.CREDIT_RESULT);
        assertThat(captor.getValue().getProviderCode()).isEqualTo(CreditProviderCode.PENDANAAN);
    }

    private static CreditApplicationRepository.CreditApplicationRecord applicationRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                10L,
                "AP-001",
                "req-1",
                CreditProviderCode.PENDANAAN,
                1L,
                "partner-1",
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
