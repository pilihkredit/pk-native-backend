package com.pk.infra.profile.mapper;
import com.pk.core.profile.ProfileDeviceData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ProfileDeviceMapper {
    boolean existsByProfileId(@Param("profileId") long profileId);
    ProfileDeviceData findByDeviceNo(@Param("deviceNo") String deviceNo);
    int upsertByDeviceNo(ProfileDeviceData data);
}
