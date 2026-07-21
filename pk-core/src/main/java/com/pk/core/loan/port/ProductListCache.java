package com.pk.core.loan.port;

import java.util.Optional;

/**
 * User-dimension product list cache (Redis). Value is {@code pk_lender_product_list.id}.
 */
public interface ProductListCache {
    Optional<Long> getProductListId(long profileId, String applyId);

    void putProductListId(long profileId, String applyId, long productListId);
}
