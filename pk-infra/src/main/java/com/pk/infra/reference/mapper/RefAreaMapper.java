package com.pk.infra.reference.mapper;
import com.pk.core.reference.AreaReference;
import com.pk.infra.reference.repository.RefAreaUpsertParam;
import java.time.Instant; import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RefAreaMapper {
    List<AreaReference> findActiveByParentCode(@Param("status") String status, @Param("parentCode") String parentCode);
    Instant findLatestSyncedAtByParent(@Param("status") String status, @Param("parentCode") String parentCode);
    AreaReference findActiveByCode(@Param("status") String status, @Param("areaCode") String areaCode);
    int countActiveByAreaCode(@Param("status") String status, @Param("areaCode") String areaCode);
    int deactivateChildrenByParent(@Param("status") String status, @Param("parentCode") String parentCode);
    int upsertArea(RefAreaUpsertParam param);
    List<String> findRefreshableParentCodes(@Param("status") String status);
}
