package com.pk.infra.loan.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.infra.loan.mapper.LoanQuoteMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanQuoteRepositoryImplTest {
    @Mock
    private LoanQuoteMapper mapper;
    @InjectMocks
    private LoanQuoteRepositoryImpl repository;

    @Test
    void upsertReplacesTermsAfterQuoteUpsert() {
        when(mapper.upsertQuote(any())).thenAnswer(invocation -> {
            LoanQuoteInsertParam param = invocation.getArgument(0);
            param.setId(42L);
            return 1;
        });

        LoanQuoteRepository.LoanQuoteInsert insert = new LoanQuoteRepository.LoanQuoteInsert(
                "QUOTE-1",
                1L,
                100L,
                "81234567890",
                88L,
                77L,
                sampleQuote(),
                Instant.parse("2026-07-22T00:00:00Z")
        );
        LenderTrialTerm term = new LenderTrialTerm(
                1,
                1747180800000L,
                1749772800000L,
                1750377600000L,
                new BigDecimal("295000"),
                new BigDecimal("250000"),
                new BigDecimal("250000"),
                new BigDecimal("45000"),
                new BigDecimal("45000"),
                new BigDecimal("295000"),
                new BigDecimal("250000"),
                new BigDecimal("45000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        LoanQuoteRepository.LoanQuoteRecord record = repository.upsert(
                insert,
                List.of(new LoanQuoteRepository.LoanQuoteTermInsert("81234567890", term))
        );

        assertThat(record.id()).isEqualTo(42L);
        assertThat(record.quoteNo()).isEqualTo("QUOTE-1");
        assertThat(record.couponId()).isEqualTo(88L);
        assertThat(record.externalInteractionId()).isEqualTo(77L);

        InOrder order = inOrder(mapper);
        order.verify(mapper).upsertQuote(any(LoanQuoteInsertParam.class));
        order.verify(mapper).deleteTermsByQuoteId(42L);
        ArgumentCaptor<LoanQuoteTermInsertParam> termCaptor = ArgumentCaptor.forClass(LoanQuoteTermInsertParam.class);
        order.verify(mapper).insertTerm(termCaptor.capture());
        assertThat(termCaptor.getValue().getQuoteId()).isEqualTo(42L);
        assertThat(termCaptor.getValue().getValueDate()).isEqualTo(1747180800000L);
        verify(mapper).deleteTermsByQuoteId(anyLong());
    }

    private static LoanTrialQuoteDetail sampleQuote() {
        return new LoanTrialQuoteDetail(
                "APPLY-1",
                "CA-1",
                "USR-1",
                new BigDecimal("1500000"),
                "PD001",
                "RP001",
                6,
                new BigDecimal("1455000"),
                new BigDecimal("1455000"),
                new BigDecimal("1455000"),
                new BigDecimal("45000"),
                new BigDecimal("1770000"),
                new BigDecimal("1770000"),
                new BigDecimal("270000"),
                new BigDecimal("0.003"),
                new BigDecimal("0.003"),
                180L,
                "Admin Fee",
                BigDecimal.ZERO,
                "Service Fee",
                BigDecimal.ZERO,
                "Insurance Fee",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "0.5",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("1500000"),
                new BigDecimal("270000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "N",
                0,
                1747200000000L,
                1749792000000L,
                1757472000000L,
                false
        );
    }
}
