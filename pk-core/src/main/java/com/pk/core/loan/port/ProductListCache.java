package com.pk.core.loan.port;

import java.util.Optional;

/**
 * User-dimension product list cache (Redis). Value is {@code pk_lender_product_list.id}.
 */
public interface ProductListCache {
    Optional<Long> getProductListId(long userId, String applyId);

    void putProductListId(long userId, String applyId, long productListId);
}
