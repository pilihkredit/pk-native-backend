package com.pk.infra.profile.mapper;
import com.pk.infra.profile.repository.ProfilePersonalRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ProfilePersonalMapper {
    ProfilePersonalRow findByProfileId(@Param("profileId") long profileId);
    int upsert(ProfilePersonalRow row);
    int updateEmail(@Param("profileId") long profileId, @Param("userEmail") String userEmail);
    int updateLastLenderInteraction(@Param("profileId") long profileId, @Param("externalInteractionId") Long externalInteractionId);
}
