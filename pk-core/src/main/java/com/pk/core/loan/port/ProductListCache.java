package com.pk.core.loan.port;

import java.util.Optional;

/**
 * User-dimension product list cache (Redis). Value is {@code snapshotNo} pointing at {@code pk_product_snapshot}.
 */
public interface ProductListCache {
    Optional<String> getSnapshotNo(long profileId, String applyId);

    void putSnapshotNo(long profileId, String applyId, String snapshotNo);
}
