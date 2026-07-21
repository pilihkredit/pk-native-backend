package com.pk.infra.loan;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.ProductListContentHash;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderProductListRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.infra.credit.CreditExternalStatusMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductListResolver {
    private final LenderProductListRepository lenderProductListRepository;
    private final ProductListCache productListCache;
    private final LenderLoanProductPort lenderLoanProductPort;
    private final Duration cacheTtl;

    public ProductListResolver(
            LenderProductListRepository lenderProductListRepository,
            ProductListCache productListCache,
            LenderLoanProductPort lenderLoanProductPort,
            LoanProductProperties loanProductProperties
    ) {
        this.lenderProductListRepository = lenderProductListRepository;
        this.productListCache = productListCache;
        this.lenderLoanProductPort = lenderLoanProductPort;
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
            Optional<ResolvedProductList> latest = resolveFromLatest(creditRecord);
            if (latest.isPresent()) {
                productListCache.putProductListId(profileId, creditRecord.applyId(), latest.get().productListId());
                return latest.get();
            }
        }
        return fetchAndPersist(profileId, creditRecord);
    }

    private Optional<ResolvedProductList> resolveFromCache(
            long profileId,
            CreditApplicationRepository.CreditApplicationRecord creditRecord
    ) {
        Optional<Long> listId = productListCache.getProductListId(profileId, creditRecord.applyId());
        if (listId.isEmpty()) {
            return Optional.empty();
        }
        return lenderProductListRepository.findById(listId.get())
                .filter(tree -> tree.header().creditApplicationId() == creditRecord.id())
                .map(this::toResolved);
    }

    private Optional<ResolvedProductList> resolveFromLatest(
            CreditApplicationRepository.CreditApplicationRecord creditRecord
    ) {
        return lenderProductListRepository.findLatestByApplyId(creditRecord.applyId())
                .filter(tree -> tree.header().creditApplicationId() == creditRecord.id())
                .filter(tree -> !isStale(tree.header().fetchedAt()))
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
        String creditStatus = CreditExternalStatusMapper.toPublicStatus(
                CreditExternalStatusMapper.mapLenderStatus(lenderResult.externalCreditStatus())
        );
        String applyId = lenderResult.applyId() == null ? creditRecord.applyId() : lenderResult.applyId();
        String contentHash = ProductListContentHash.sha256(
                creditStatus,
                productStatus,
                lenderResult.creditApplyNo(),
                lenderResult.userId(),
                lenderResult.products()
        );

        Optional<LenderProductListRepository.ProductListTree> latest =
                lenderProductListRepository.findLatestByApplyId(creditRecord.applyId())
                        .filter(tree -> tree.header().creditApplicationId() == creditRecord.id());

        LenderProductListRepository.ProductListTree tree;
        if (latest.isPresent() && Objects.equals(latest.get().header().contentHash(), contentHash)) {
            tree = latest.get();
        } else {
            tree = lenderProductListRepository.insertTree(new LenderProductListRepository.ProductListInsert(
                    profileId,
                    creditRecord.id(),
                    creditRecord.mobileNo(),
                    applyId,
                    lenderResult.creditApplyNo(),
                    lenderResult.userId(),
                    creditStatus,
                    productStatus,
                    contentHash,
                    lenderResult.externalInteractionId(),
                    fetchedAt,
                    lenderResult.products()
            ));
        }
        productListCache.putProductListId(profileId, creditRecord.applyId(), tree.header().id());
        return toResolved(tree);
    }

    private ResolvedProductList toResolved(LenderProductListRepository.ProductListTree tree) {
        return new ResolvedProductList(
                tree.header().id(),
                tree.header().creditStatus(),
                tree.header().productStatus(),
                tree.products(),
                tree.header().fetchedAt()
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
            long productListId,
            String creditStatus,
            String productStatus,
            List<LenderLoanProduct> products,
            Instant fetchedAt
    ) {
    }
}
