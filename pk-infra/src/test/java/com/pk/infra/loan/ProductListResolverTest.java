package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.core.loan.port.ProductSnapshotRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductListResolverTest {
    @Mock
    private ProductSnapshotRepository productSnapshotRepository;
    @Mock
    private ProductListCache productListCache;
    @Mock
    private LenderLoanProductPort lenderLoanProductPort;
    @Mock
    private LenderProductLatestRepository lenderProductLatestRepository;

    private ProductListResolver resolver;

    @BeforeEach
    void setUp() {
        LoanProductProperties properties = new LoanProductProperties();
        properties.setCacheTtl(Duration.ofSeconds(30));
        resolver = new ProductListResolver(
                productSnapshotRepository,
                productListCache,
                lenderLoanProductPort,
                lenderProductLatestRepository,
                new ProductSnapshotPayloadCodec(new ObjectMapper()),
                properties
        );
    }

    @Test
    void returnsCachedSnapshotWithoutCallingLender() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        ProductSnapshotRepository.ProductSnapshotRecord snapshot = freshSnapshot(record, "READY");
        when(productListCache.getSnapshotNo(1L, "APPLY-1")).thenReturn(Optional.of(snapshot.snapshotNo()));
        when(productSnapshotRepository.findBySnapshotNo(snapshot.snapshotNo())).thenReturn(Optional.of(snapshot));

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, false);

        assertThat(result.snapshotNo()).isEqualTo(snapshot.snapshotNo());
        assertThat(result.productStatus()).isEqualTo("READY");
        verify(lenderLoanProductPort, never()).listProducts(any());
        verify(lenderProductLatestRepository, never()).replaceLatest(any());
    }

    @Test
    void forceRefreshBypassesCacheAndPersistsSnapshot() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        when(lenderLoanProductPort.listProducts("APPLY-1")).thenReturn(lenderProducts());
        when(productSnapshotRepository.insert(any())).thenAnswer(invocation -> {
            ProductSnapshotRepository.ProductSnapshotInsert command = invocation.getArgument(0);
            return new ProductSnapshotRepository.ProductSnapshotRecord(
                    501L,
                    command.snapshotNo(),
                    command.applyId(),
                    command.creditApplicationId(),
                    command.creditStatus(),
                    command.productStatus(),
                    command.productsJson(),
                    command.fetchedAt()
            );
        });

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, true);

        assertThat(result.snapshotId()).isEqualTo(501L);
        assertThat(result.products()).hasSize(1);
        verify(lenderLoanProductPort).listProducts("APPLY-1");
        verify(productListCache).putSnapshotNo(eq(1L), eq("APPLY-1"), any());
        verify(productSnapshotRepository).insert(any());
        verify(lenderProductLatestRepository).replaceLatest(any());
    }

    @Test
    void refetchesWhenCachedSnapshotIsStale() {
        CreditApplicationRepository.CreditApplicationRecord record = approvedRecord();
        ProductSnapshotRepository.ProductSnapshotRecord stale = freshSnapshot(
                record,
                "READY",
                Instant.now().minusSeconds(60)
        );
        when(productListCache.getSnapshotNo(1L, "APPLY-1")).thenReturn(Optional.of(stale.snapshotNo()));
        when(productSnapshotRepository.findBySnapshotNo(stale.snapshotNo())).thenReturn(Optional.of(stale));
        when(productSnapshotRepository.findLatestByCreditApplicationId(100L)).thenReturn(Optional.of(stale));
        when(lenderLoanProductPort.listProducts("APPLY-1")).thenReturn(lenderProducts());
        when(productSnapshotRepository.insert(any())).thenAnswer(invocation -> {
            ProductSnapshotRepository.ProductSnapshotInsert command = invocation.getArgument(0);
            return new ProductSnapshotRepository.ProductSnapshotRecord(
                    502L,
                    command.snapshotNo(),
                    command.applyId(),
                    command.creditApplicationId(),
                    command.creditStatus(),
                    command.productStatus(),
                    command.productsJson(),
                    command.fetchedAt()
            );
        });

        ProductListResolver.ResolvedProductList result = resolver.resolve(1L, record, false);

        assertThat(result.snapshotId()).isEqualTo(502L);
        verify(lenderLoanProductPort).listProducts("APPLY-1");
        verify(lenderProductLatestRepository).replaceLatest(any());
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
                9L,
                "CA-1",
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                null
        );
    }

    private static ProductSnapshotRepository.ProductSnapshotRecord freshSnapshot(
            CreditApplicationRepository.CreditApplicationRecord record,
            String productStatus
    ) {
        return freshSnapshot(record, productStatus, Instant.now());
    }

    private static ProductSnapshotRepository.ProductSnapshotRecord freshSnapshot(
            CreditApplicationRepository.CreditApplicationRecord record,
            String productStatus,
            Instant fetchedAt
    ) {
        String productsJson = """
                {"productStatus":"READY","products":[{"productCode":"PD001","productName":"Cash Loan",\
                "minAmount":500000,"maxAmount":3000000,"comprehensiveRateUnit":null,"comprehensiveRate":0.18,\
                "repayMethods":[{"repayMethod":"RP001","cycleType":"D","cycleInterval":30,"cycleCount":6,\
                "totalCycleInterval":180,"repayMethodType":0,"unevenBillsRepaymentRates":[]}]}]}
                """;
        return new ProductSnapshotRepository.ProductSnapshotRecord(
                500L,
                "PSNAP20250624120000AAAAAA",
                record.applyId(),
                record.id(),
                "APPROVED",
                productStatus,
                productsJson,
                fetchedAt
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
                ))
        );
    }
}
