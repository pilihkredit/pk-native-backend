package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.ProductListContentHash;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderProductListRepository;
import com.pk.core.loan.port.ProductListCache;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductListResolverTest {
    @Mock
    private LenderProductListRepository lenderProductListRepository;
    @Mock
    private ProductListCache productListCache;
    @Mock
    private LenderLoanProductPort lenderLoanProductPort;

    private ProductListResolver resolver;

    @BeforeEach
    void setUp() {
        LoanProductProperties properties = new LoanProductProperties();
        properties.setCacheTtl(Duration.ofSeconds(30));
        resolver = new ProductListResolver(
                lenderProductListRepository,
                productListCache,
                lenderLoanProductPort,
                properties
        );
    }

    @Test
    void returnsCachedListWithoutCallingLender() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        LenderProductListRepository.ProductListTree tree = freshTree(record, "READY", Instant.now());
        when(productListCache.getProductListId(1L, "APPLY-1")).thenReturn(Optional.of(tree.header().id()));
        when(lenderProductListRepository.findById(tree.header().id())).thenReturn(Optional.of(tree));

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, false);

        assertThat(result.productListId()).isEqualTo(tree.header().id());
        assertThat(result.productStatus()).isEqualTo("READY");
        verify(lenderLoanProductPort, never()).listProducts(any());
        verify(lenderProductListRepository, never()).insertTree(any());
    }

    @Test
    void forceRefreshInsertsWhenContentChanged() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        when(lenderLoanProductPort.listProducts("APPLY-1")).thenReturn(lenderProducts());
        when(lenderProductListRepository.findLatestByApplyId("APPLY-1")).thenReturn(Optional.empty());
        when(lenderProductListRepository.insertTree(any())).thenAnswer(invocation -> {
            LenderProductListRepository.ProductListInsert command = invocation.getArgument(0);
            return new LenderProductListRepository.ProductListTree(
                    new LenderProductListRepository.ProductListHeader(
                            501L,
                            command.userId(),
                            command.creditApplicationId(),
                            command.applyId(),
                            command.creditApplyNo(),
                            command.lenderUserId(),
                            command.creditStatus(),
                            command.productStatus(),
                            command.contentHash(),
                            command.externalInteractionId(),
                            command.fetchedAt()
                    ),
                    command.products()
            );
        });

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, true);

        assertThat(result.productListId()).isEqualTo(501L);
        assertThat(result.products()).hasSize(1);
        verify(lenderLoanProductPort).listProducts("APPLY-1");
        verify(productListCache).putProductListId(eq(1L), eq("APPLY-1"), eq(501L));
        ArgumentCaptor<LenderProductListRepository.ProductListInsert> insertCaptor =
                ArgumentCaptor.forClass(LenderProductListRepository.ProductListInsert.class);
        verify(lenderProductListRepository).insertTree(insertCaptor.capture());
        assertThat(insertCaptor.getValue().externalInteractionId()).isEqualTo(88L);
        assertThat(insertCaptor.getValue().contentHash()).isEqualTo(ProductListContentHash.sha256(
                insertCaptor.getValue().creditStatus(),
                insertCaptor.getValue().productStatus(),
                insertCaptor.getValue().creditApplyNo(),
                insertCaptor.getValue().lenderUserId(),
                insertCaptor.getValue().products()
        ));
    }

    @Test
    void reusesLatestWhenContentHashUnchanged() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        LenderLoanProductPort.LenderLoanProductListResult lenderResult = lenderProducts();
        String creditStatus = "APPROVED";
        String productStatus = "READY";
        String hash = ProductListContentHash.sha256(
                creditStatus,
                productStatus,
                lenderResult.creditApplyNo(),
                lenderResult.userId(),
                lenderResult.products()
        );
        LenderProductListRepository.ProductListTree existing = freshTree(record, productStatus, Instant.now().minusSeconds(60), hash);
        when(productListCache.getProductListId(1L, "APPLY-1")).thenReturn(Optional.empty());
        when(lenderProductListRepository.findLatestByApplyId("APPLY-1")).thenReturn(Optional.of(existing));
        when(lenderLoanProductPort.listProducts("APPLY-1")).thenReturn(lenderResult);

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, false);

        assertThat(result.productListId()).isEqualTo(existing.header().id());
        verify(lenderProductListRepository, never()).insertTree(any());
        verify(productListCache).putProductListId(1L, "APPLY-1", existing.header().id());
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                "CA-1"
        );
    }

    private static LenderProductListRepository.ProductListTree freshTree(
            CreditApplicationRepository.CreditApplicationRecord record,
            String productStatus,
            Instant fetchedAt
    ) {
        return freshTree(record, productStatus, fetchedAt, "hash-1");
    }

    private static LenderProductListRepository.ProductListTree freshTree(
            CreditApplicationRepository.CreditApplicationRecord record,
            String productStatus,
            Instant fetchedAt,
            String contentHash
    ) {
        List<LenderLoanProduct> products = List.of(new LenderLoanProduct(
                "PD001",
                null,
                new BigDecimal("500000"),
                new BigDecimal("3000000"),
                null,
                new BigDecimal("0.18"),
                List.of(new LenderRepayMethod("RP001", "D", 30, 6, 180, 0, null, List.of()))
        ));
        return new LenderProductListRepository.ProductListTree(
                new LenderProductListRepository.ProductListHeader(
                        500L,
                        record.userId(),
                        record.id(),
                        record.applyId(),
                        "CA-1",
                        "USR-1",
                        "APPROVED",
                        productStatus,
                        contentHash,
                        1L,
                        fetchedAt
                ),
                products
        );
    }

    private static LenderLoanProductPort.LenderLoanProductListResult lenderProducts() {
        return new LenderLoanProductPort.LenderLoanProductListResult(
                "APPLY-1",
                "CA-1",
                "USR-1",
                "SUCCESS",
                "READY",
                List.of(new LenderLoanProduct(
                        "PD001",
                        "Cash Loan",
                        new BigDecimal("500000"),
                        new BigDecimal("3000000"),
                        null,
                        new BigDecimal("0.18"),
                        List.of(new LenderRepayMethod("RP001", "D", 30, 6, 180, 0, null, List.of()))
                )),
                88L
        );
    }
}
