package com.pk.infra.loan.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.infra.loan.mapper.LenderProductLatestMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LenderProductLatestRepositoryImplTest {
    @Mock
    private LenderProductLatestMapper mapper;

    private LenderProductLatestRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LenderProductLatestRepositoryImpl(mapper);
    }

    @Test
    void replaceLatestClearsExistingChildrenBeforeInsertingNewRows() {
        when(mapper.findIdByCreditApplicationId(100L)).thenReturn(9L, 9L);
        when(mapper.insertProduct(any())).thenAnswer(invocation -> {
            LenderProductLatestInsertParam param = invocation.getArgument(0);
            param.setId(501L);
            return 1;
        });
        when(mapper.insertRepayMethod(any())).thenAnswer(invocation -> {
            LenderProductRepayMethodLatestInsertParam param = invocation.getArgument(0);
            param.setId(601L);
            return 1;
        });

        repository.replaceLatest(firstCommand());

        InOrder order = inOrder(mapper);
        order.verify(mapper).deleteUnevenRatesByListLatestId(9L);
        order.verify(mapper).deleteRepayMethodsByListLatestId(9L);
        order.verify(mapper).deleteProductsByListLatestId(9L);
        order.verify(mapper).upsertListLatest(any());
        order.verify(mapper).insertProduct(any());
        order.verify(mapper).insertRepayMethod(any());
        order.verify(mapper, org.mockito.Mockito.times(2)).insertUnevenRate(any());

        ArgumentCaptor<LenderProductLatestInsertParam> productCaptor =
                ArgumentCaptor.forClass(LenderProductLatestInsertParam.class);
        verify(mapper).insertProduct(productCaptor.capture());
        assertThat(productCaptor.getValue().getMobileNo()).isEqualTo("81234567890");
        assertThat(productCaptor.getValue().getProductCode()).isEqualTo("PD001");
        assertThat(productCaptor.getValue().getComprehensiveRateUnit()).isEqualTo("M");

        ArgumentCaptor<LenderProductRepayMethodLatestInsertParam> methodCaptor =
                ArgumentCaptor.forClass(LenderProductRepayMethodLatestInsertParam.class);
        verify(mapper).insertRepayMethod(methodCaptor.capture());
        assertThat(methodCaptor.getValue().getMobileNo()).isEqualTo("81234567890");
        assertThat(methodCaptor.getValue().getUnevenBillsRepaymentRateJson()).contains("termNum");

        ArgumentCaptor<LenderProductUnevenRateLatestInsertParam> rateCaptor =
                ArgumentCaptor.forClass(LenderProductUnevenRateLatestInsertParam.class);
        verify(mapper, org.mockito.Mockito.times(2)).insertUnevenRate(rateCaptor.capture());
        assertThat(rateCaptor.getAllValues())
                .allSatisfy(rate -> assertThat(rate.getMobileNo()).isEqualTo("81234567890"));

        ArgumentCaptor<LenderProductLatestRepository.ReplaceLatestCommand> commandCaptor =
                ArgumentCaptor.forClass(LenderProductLatestRepository.ReplaceLatestCommand.class);
        verify(mapper).upsertListLatest(commandCaptor.capture());
        assertThat(commandCaptor.getValue().lastLenderRequestJson()).contains("APPLY-1");
        assertThat(commandCaptor.getValue().lastLenderResponseJson()).contains("PD001");
    }

    @Test
    void replaceLatestInsertsWithoutDeleteWhenNoExistingHeader() {
        when(mapper.findIdByCreditApplicationId(100L)).thenReturn(null, 10L);
        when(mapper.insertProduct(any())).thenAnswer(invocation -> {
            LenderProductLatestInsertParam param = invocation.getArgument(0);
            param.setId(502L);
            return 1;
        });

        repository.replaceLatest(new LenderProductLatestRepository.ReplaceLatestCommand(
                1L,
                100L,
                "81234567891",
                "APPLY-2",
                "CA-2",
                "USR-2",
                "SUCCESS",
                "READY",
                "{\"applyId\":\"APPLY-2\"}",
                "{\"products\":[{\"productCode\":\"PD002\"}]}",
                Instant.now(),
                List.of(new LenderLoanProduct(
                        "PD002",
                        "New Loan",
                        new BigDecimal("100000"),
                        new BigDecimal("2000000"),
                        "Y",
                        new BigDecimal("0.20"),
                        List.of()
                ))
        ));

        verify(mapper, never()).deleteProductsByListLatestId(anyLong());
        verify(mapper).upsertListLatest(any());
        verify(mapper).insertProduct(any());
    }

    private static LenderProductLatestRepository.ReplaceLatestCommand firstCommand() {
        return new LenderProductLatestRepository.ReplaceLatestCommand(
                1L,
                100L,
                "81234567890",
                "APPLY-1",
                "CA-1",
                "USR-1",
                "SUCCESS",
                "READY",
                "{\"applyId\":\"APPLY-1\"}",
                "{\"products\":[{\"productCode\":\"PD001\"}]}",
                Instant.parse("2026-06-24T04:00:00Z"),
                List.of(new LenderLoanProduct(
                        "PD001",
                        "Cash Loan",
                        new BigDecimal("500000"),
                        new BigDecimal("3000000"),
                        "M",
                        new BigDecimal("0.18"),
                        List.of(new LenderRepayMethod(
                                "RP001",
                                "D",
                                15,
                                6,
                                6,
                                1,
                                "[{\"termNum\":1,\"repaymentRate\":0.6},{\"termNum\":2,\"repaymentRate\":0.4}]",
                                List.of(
                                        new LenderRepayMethod.UnevenBillRate(1, new BigDecimal("0.6")),
                                        new LenderRepayMethod.UnevenBillRate(2, new BigDecimal("0.4"))
                                )
                        ))
                ))
        );
    }
}
