package com.pk.infra.loan.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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
    void insertsWhenNoExistingQuote() {
        when(mapper.findLatestByApplyProductRepay("APPLY-1", "PD001", "RP001")).thenReturn(null);
        when(mapper.insertQuote(any())).thenAnswer(invocation -> {
            LoanQuoteInsertParam param = invocation.getArgument(0);
            param.setId(42L);
            return 1;
        });

        LoanQuoteRepository.LoanQuoteRecord record = repository.upsert(sampleInsert("QUOTE-NEW"), List.of(sampleTerm()));

        assertThat(record.id()).isEqualTo(42L);
        assertThat(record.quoteNo()).isEqualTo("QUOTE-NEW");
        verify(mapper).insertQuote(any(LoanQuoteInsertParam.class));
        verify(mapper, never()).updateQuoteById(any());
        verify(mapper).deleteTermsByQuoteId(42L);
        verify(mapper).insertTerm(any(LoanQuoteTermInsertParam.class));
    }

    @Test
    void overwritesDraftAndRefreshesQuoteNo() {
        LoanQuoteRow draft = new LoanQuoteRow();
        draft.setId(10L);
        draft.setQuoteNo("QUOTE-OLD");
        when(mapper.findLatestByApplyProductRepay("APPLY-1", "PD001", "RP001")).thenReturn(draft);
        when(mapper.countLoanApplicationReferences(10L, "QUOTE-OLD")).thenReturn(0);
        when(mapper.updateQuoteById(any())).thenReturn(1);

        LoanQuoteRepository.LoanQuoteRecord record = repository.upsert(sampleInsert("QUOTE-NEW"), List.of(sampleTerm()));

        assertThat(record.id()).isEqualTo(10L);
        assertThat(record.quoteNo()).isEqualTo("QUOTE-NEW");
        ArgumentCaptor<LoanQuoteInsertParam> captor = ArgumentCaptor.forClass(LoanQuoteInsertParam.class);
        verify(mapper).updateQuoteById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getQuoteNo()).isEqualTo("QUOTE-NEW");
        verify(mapper, never()).insertQuote(any());
        InOrder order = inOrder(mapper);
        order.verify(mapper).updateQuoteById(any());
        order.verify(mapper).deleteTermsByQuoteId(10L);
        order.verify(mapper).insertTerm(any());
    }

    @Test
    void insertsNewWhenLatestQuoteAlreadyReferencedByLoanApplication() {
        LoanQuoteRow locked = new LoanQuoteRow();
        locked.setId(10L);
        locked.setQuoteNo("QUOTE-LOCKED");
        when(mapper.findLatestByApplyProductRepay("APPLY-1", "PD001", "RP001")).thenReturn(locked);
        when(mapper.countLoanApplicationReferences(10L, "QUOTE-LOCKED")).thenReturn(1);
        when(mapper.insertQuote(any())).thenAnswer(invocation -> {
            LoanQuoteInsertParam param = invocation.getArgument(0);
            param.setId(99L);
            return 1;
        });

        LoanQuoteRepository.LoanQuoteRecord record = repository.upsert(sampleInsert("QUOTE-NEW"), List.of(sampleTerm()));

        assertThat(record.id()).isEqualTo(99L);
        assertThat(record.quoteNo()).isEqualTo("QUOTE-NEW");
        verify(mapper).insertQuote(any(LoanQuoteInsertParam.class));
        verify(mapper, never()).updateQuoteById(any());
        verify(mapper).deleteTermsByQuoteId(99L);
        verify(mapper, never()).deleteTermsByQuoteId(eq(10L));
        verify(mapper).deleteTermsByQuoteId(anyLong());
    }

    private static LoanQuoteRepository.LoanQuoteInsert sampleInsert(String quoteNo) {
        return new LoanQuoteRepository.LoanQuoteInsert(
                quoteNo,
                1L,
                100L,
                88L,
                77L,
                sampleQuote(),
                Instant.parse("2026-07-22T00:00:00Z")
        );
    }

    private static LoanQuoteRepository.LoanQuoteTermInsert sampleTerm() {
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
        return new LoanQuoteRepository.LoanQuoteTermInsert(1L, term);
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
