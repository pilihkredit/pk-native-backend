package com.pk.infra.loan.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderProductListRepository;
import com.pk.infra.loan.mapper.LenderProductListMapper;
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
class LenderProductListRepositoryImplTest {
    @Mock
    private LenderProductListMapper mapper;

    private LenderProductListRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LenderProductListRepositoryImpl(mapper);
    }

    @Test
    void insertTreeWritesHeaderThenChildren() {
        when(mapper.insertList(any())).thenAnswer(invocation -> {
            LenderProductListInsertParam param = invocation.getArgument(0);
            param.setId(9L);
            return 1;
        });
        when(mapper.insertProduct(any())).thenAnswer(invocation -> {
            LenderProductInsertParam param = invocation.getArgument(0);
            param.setId(501L);
            return 1;
        });
        when(mapper.insertRepayMethod(any())).thenAnswer(invocation -> {
            LenderProductRepayMethodInsertParam param = invocation.getArgument(0);
            param.setId(601L);
            return 1;
        });
        when(mapper.findHeaderById(9L)).thenReturn(headerRow(9L));
        when(mapper.findProductsByListId(9L)).thenReturn(List.of(productRow(501L, 9L)));
        when(mapper.findRepayMethodsByProductIds(anyList())).thenReturn(List.of(methodRow(601L, 501L)));
        when(mapper.findUnevenRatesByRepayMethodIds(anyList())).thenReturn(List.of(
                rateRow(701L, 601L, 1, "0.6"),
                rateRow(702L, 601L, 2, "0.4")
        ));

        LenderProductListRepository.ProductListTree tree = repository.insertTree(command());

        InOrder order = inOrder(mapper);
        order.verify(mapper).insertList(any());
        order.verify(mapper).insertProduct(any());
        order.verify(mapper).insertRepayMethod(any());
        order.verify(mapper, org.mockito.Mockito.times(2)).insertUnevenRate(any());

        ArgumentCaptor<LenderProductInsertParam> productCaptor =
                ArgumentCaptor.forClass(LenderProductInsertParam.class);
        verify(mapper).insertProduct(productCaptor.capture());
        assertThat(productCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(productCaptor.getValue().getProductCode()).isEqualTo("PD001");
        assertThat(productCaptor.getValue().getComprehensiveRateUnit()).isEqualTo("M");

        assertThat(tree.header().id()).isEqualTo(9L);
        assertThat(tree.products()).hasSize(1);
        assertThat(tree.products().getFirst().repayMethods().getFirst().unevenBillsRepaymentRates()).hasSize(2);
    }

    private static LenderProductListRepository.ProductListInsert command() {
        return new LenderProductListRepository.ProductListInsert(
                1L,
                100L,
                "APPLY-1",
                "CA-1",
                "USR-1",
                "APPROVED",
                "READY",
                "abc",
                88L,
                Instant.parse("2026-07-21T00:00:00Z"),
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
                                "[{\"termNum\":1}]",
                                List.of(
                                        new LenderRepayMethod.UnevenBillRate(1, new BigDecimal("0.6")),
                                        new LenderRepayMethod.UnevenBillRate(2, new BigDecimal("0.4"))
                                )
                        ))
                ))
        );
    }

    private static LenderProductListHeaderRow headerRow(long id) {
        LenderProductListHeaderRow row = new LenderProductListHeaderRow();
        row.setId(id);
        row.setUserId(1L);
        row.setCreditApplicationId(100L);
        row.setApplyId("APPLY-1");
        row.setCreditApplyNo("CA-1");
        row.setLenderUserId("USR-1");
        row.setCreditStatus("APPROVED");
        row.setProductStatus("READY");
        row.setContentHash("abc");
        row.setExternalInteractionId(88L);
        row.setFetchedAt(Instant.parse("2026-07-21T00:00:00Z"));
        return row;
    }

    private static LenderProductRow productRow(long id, long listId) {
        LenderProductRow row = new LenderProductRow();
        row.setId(id);
        row.setProductListId(listId);
        row.setProductCode("PD001");
        row.setMinAmount(new BigDecimal("500000"));
        row.setMaxAmount(new BigDecimal("3000000"));
        row.setComprehensiveRateUnit("M");
        row.setComprehensiveRate(new BigDecimal("0.18"));
        return row;
    }

    private static LenderProductRepayMethodRow methodRow(long id, long productId) {
        LenderProductRepayMethodRow row = new LenderProductRepayMethodRow();
        row.setId(id);
        row.setProductId(productId);
        row.setRepayMethod("RP001");
        row.setCycleType("D");
        row.setCycleInterval(15);
        row.setCycleCount(6);
        row.setTotalCycleInterval(6);
        row.setRepayMethodType(1);
        return row;
    }

    private static LenderProductUnevenRateRow rateRow(long id, long methodId, int termNum, String rate) {
        LenderProductUnevenRateRow row = new LenderProductUnevenRateRow();
        row.setId(id);
        row.setRepayMethodId(methodId);
        row.setTermNum(termNum);
        row.setRepaymentRate(new BigDecimal(rate));
        return row;
    }
}
