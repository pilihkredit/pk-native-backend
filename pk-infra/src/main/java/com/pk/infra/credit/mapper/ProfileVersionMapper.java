package com.pk.infra.credit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileVersionMapper {
    int nextVersionNo(@Param("profileId") long profileId);

    int insert(
            @Param("profileId") long profileId,
            @Param("mobileNo") String mobileNo,
            @Param("versionNo") int versionNo,
            @Param("snapshotHash") String snapshotHash,
            @Param("snapshotJson") String snapshotJson,
            @Param("completedModulesJson") String completedModulesJson,
            @Param("legalBasis") String legalBasis,
            @Param("processingPurpose") String processingPurpose,
            @Param("source") String source
    );

    Long findIdByProfileIdAndVersionNo(@Param("profileId") long profileId, @Param("versionNo") int versionNo);
}
