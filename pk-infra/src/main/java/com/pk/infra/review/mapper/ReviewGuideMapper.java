package com.pk.infra.review.mapper;

import com.pk.infra.review.repository.ReviewGuideExposureInsertParam;
import com.pk.infra.review.repository.ReviewGuideExposureRow;
import com.pk.infra.review.repository.ReviewGuideUserStateRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewGuideMapper {
    int ensureUserState(@Param("userId") long userId);

    ReviewGuideUserStateRow findUserStateForUpdate(@Param("userId") long userId);

    ReviewGuideExposureRow findByUserIdAndScene(
            @Param("userId") long userId,
            @Param("scene") String scene
    );

    int insertExposure(ReviewGuideExposureInsertParam param);

    ReviewGuideExposureRow findByIdAndUserIdForUpdate(
            @Param("id") long id,
            @Param("userId") long userId
    );

    int markStoreJumpIfAbsent(@Param("userId") long userId);

    int saveRatingIfAbsent(@Param("id") long id, @Param("rating") int rating);
}
