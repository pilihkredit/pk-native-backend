package com.pk.infra.loan;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.core.loan.port.ProductSnapshotRepository;
import com.pk.infra.credit.CreditExternalStatusMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ProductListResolver {
    private final ProductSnapshotRepository productSnapshotRepository;
    private final ProductListCache productListCache;
    private final LenderLoanProductPort lenderLoanProductPort;
    private final LenderProductLatestRepository lenderProductLatestRepository;
    private final ProductSnapshotPayloadCodec payloadCodec;
    private final Duration cacheTtl;

    public ProductListResolver(
            ProductSnapshotRepository productSnapshotRepository,
            ProductListCache productListCache,
            LenderLoanProductPort lenderLoanProductPort,
            LenderProductLatestRepository lenderProductLatestRepository,
            ProductSnapshotPayloadCodec payloadCodec,
            LoanProductProperties loanProductProperties
    ) {
        this.productSnapshotRepository = productSnapshotRepository;
        this.productListCache = productListCache;
        this.lenderLoanProductPort = lenderLoanProductPort;
        this.lenderProductLatestRepository = lenderProductLatestRepository;
        this.payloadCodec = payloadCodec;
        this.cacheTtl = loanProductProperties.cacheTtl();
    }

    public ResolvedProductList resolve(
            long profileId,
            CreditApplicationRepository.CreditApplicationRecord creditRecord,
            boolean forceRefresh
    ) {
        if (!forceRefresh) {
            Optional<ResolvedProductList> cached = resolveFromCache(profileId, creditRecord);
            if (cached.isPresent()) {
                return cached.get();
            }
            Optional<ResolvedProductList> latest = resolveFromLatestSnapshot(creditRecord);
            if (latest.isPresent()) {
                productListCache.putSnapshotNo(profileId, creditRecord.applyId(), latest.get().snapshotNo());
                return latest.get();
            }
        }
        return fetchAndPersist(profileId, creditRecord);
    }

    private Optional<ResolvedProductList> resolveFromCache(
            long profileId,
            CreditApplicationRepository.CreditApplicationRecord creditRecord
    ) {
        Optional<String> snapshotNo = productListCache.getSnapshotNo(profileId, creditRecord.applyId());
        if (snapshotNo.isEmpty()) {
            return Optional.empty();
        }
        return productSnapshotRepository.findBySnapshotNo(snapshotNo.get())
                .filter(snapshot -> snapshot.creditApplicationId() == creditRecord.id())
                .filter(snapshot -> !isStale(snapshot.fetchedAt()))
                .map(this::toResolved);
    }

    private Optional<ResolvedProductList> resolveFromLatestSnapshot(
            CreditApplicationRepository.CreditApplicationRecord creditRecord
    ) {
        return productSnapshotRepository.findLatestByCreditApplicationId(creditRecord.id())
                .filter(snapshot -> !isStale(snapshot.fetchedAt()))
                .map(this::toResolved);
    }

    private ResolvedProductList fetchAndPersist(
            long profileId,
            CreditApplicationRepository.CreditApplicationRecord creditRecord
    ) {
        LenderLoanProductPort.LenderLoanProductListResult lenderResult =
                lenderLoanProductPort.listProducts(creditRecord.applyId());
        Instant fetchedAt = Instant.now();
        String productStatus = mapProductStatus(lenderResult.productStatus());
        String productsJson = payloadCodec.encode(productStatus, lenderResult.products());
        String snapshotNo = ProductSnapshotNoGenerator.generate();
        ProductSnapshotRepository.ProductSnapshotRecord snapshot = productSnapshotRepository.insert(
                new ProductSnapshotRepository.ProductSnapshotInsert(
                        snapshotNo,
                        creditRecord.applyId(),
                        creditRecord.id(),
                        CreditExternalStatusMapper.publicStatusOf(creditRecord),
                        productStatus,
                        productsJson,
                        fetchedAt
                )
        );
        lenderProductLatestRepository.replaceLatest(new LenderProductLatestRepository.ReplaceLatestCommand(
                profileId,
                creditRecord.id(),
                creditRecord.mobileNo(),
                lenderResult.applyId() == null ? creditRecord.applyId() : lenderResult.applyId(),
                lenderResult.creditApplyNo(),
                lenderResult.userId(),
                lenderResult.externalCreditStatus(),
                productStatus,
                lenderResult.requestJson(),
                lenderResult.responseDataJson(),
                fetchedAt,
                lenderResult.products()
        ));
        productListCache.putSnapshotNo(profileId, creditRecord.applyId(), snapshot.snapshotNo());
        return new ResolvedProductList(
                snapshot.id(),
                snapshot.snapshotNo(),
                snapshot.creditStatus(),
                productStatus,
                lenderResult.products(),
                snapshot.fetchedAt()
        );
    }

    private ResolvedProductList toResolved(ProductSnapshotRepository.ProductSnapshotRecord snapshot) {
        ProductSnapshotPayloadCodec.StoredPayload payload = payloadCodec.decode(snapshot.productsJson());
        return new ResolvedProductList(
                snapshot.id(),
                snapshot.snapshotNo(),
                snapshot.creditStatus(),
                snapshot.productStatus(),
                payload.toLenderProducts(),
                snapshot.fetchedAt()
        );
    }

    private boolean isStale(Instant fetchedAt) {
        return fetchedAt.plus(cacheTtl).isBefore(Instant.now());
    }

    private static String mapProductStatus(String lenderStatus) {
        if (lenderStatus == null || lenderStatus.isBlank()) {
            return "EMPTY";
        }
        return switch (lenderStatus.trim().toUpperCase()) {
            case "READY" -> "READY";
            case "PENDING" -> "PENDING";
            default -> "EMPTY";
        };
    }

    public record ResolvedProductList(
            long snapshotId,
            String snapshotNo,
            String creditStatus,
            String productStatus,
            List<LenderLoanProduct> products,
            Instant fetchedAt
    ) {
    }
}
