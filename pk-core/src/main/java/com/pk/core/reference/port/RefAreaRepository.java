package com.pk.core.reference.port;

import com.pk.core.reference.AreaReference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefAreaRepository {
    List<AreaReference> findActiveByParentCode(String parentCode);

    Optional<Instant> findLatestSyncedAtByParent(String parentCode);

    Optional<AreaReference> findActiveByCode(String areaCode);

    boolean existsActiveAreaCode(String areaCode);

    void replaceChildrenByParent(String parentCode, List<AreaReference> areas, Instant syncedAt);

    /** Province and city codes that may have child nodes to refresh from the lender. */
    List<String> findRefreshableParentCodes();
}
