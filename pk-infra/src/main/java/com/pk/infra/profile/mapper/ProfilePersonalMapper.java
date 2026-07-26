package com.pk.infra.profile.mapper;
import com.pk.infra.profile.repository.ProfilePersonalRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ProfilePersonalMapper {
    ProfilePersonalRow findByUserId(@Param("userId") long userId);
    int upsert(ProfilePersonalRow row);
    int updateEmail(@Param("userId") long userId, @Param("userEmail") String userEmail);
    int updateLastLenderInteraction(@Param("userId") long userId, @Param("externalInteractionId") Long externalInteractionId);
}
