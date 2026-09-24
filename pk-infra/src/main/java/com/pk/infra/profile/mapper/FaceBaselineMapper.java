package com.pk.infra.profile.mapper;

import com.pk.infra.profile.repository.FaceBaselineCandidateRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FaceBaselineMapper {
    FaceBaselineCandidateRow findLatestSuccessfulCompareByUserId(@Param("userId") long userId);
}
