package com.pk.core.loan.port;

import com.pk.core.loan.LenderLoanProduct;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LenderProductListRepository {
    Optional<ProductListTree> findById(long id);

    Optional<ProductListTree> findLatestByApplyId(String applyId);

    ProductListTree insertTree(ProductListInsert command);

    record ProductListInsert(
            long userId,
            long creditApplicationId,
            String applyId,
            String creditApplyNo,
            String lenderUserId,
            String creditStatus,
            String productStatus,
            String contentHash,
            Long externalInteractionId,
            Instant fetchedAt,
            List<LenderLoanProduct> products
    ) {
    }

    record ProductListHeader(
            long id,
            long userId,
            long creditApplicationId,
            String applyId,
            String creditApplyNo,
            String lenderUserId,
            String creditStatus,
            String productStatus,
            String contentHash,
            Long externalInteractionId,
            Instant fetchedAt
    ) {
    }

    record ProductListTree(
            ProductListHeader header,
            List<LenderLoanProduct> products
    ) {
    }
}
