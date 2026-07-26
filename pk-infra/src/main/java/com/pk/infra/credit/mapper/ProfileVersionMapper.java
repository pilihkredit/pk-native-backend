package com.pk.infra.credit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileVersionMapper {
    int nextVersionNo(@Param("userId") long userId);

    int insert(
            @Param("userId") long userId,
            @Param("mobileNo") String mobileNo,
            @Param("versionNo") int versionNo,
            @Param("snapshotHash") String snapshotHash,
            @Param("snapshotJson") String snapshotJson,
            @Param("completedModulesJson") String completedModulesJson,
            @Param("legalBasis") String legalBasis,
            @Param("processingPurpose") String processingPurpose,
            @Param("source") String source
    );

    Long findIdByUserIdAndVersionNo(@Param("userId") long userId, @Param("versionNo") int versionNo);
}
