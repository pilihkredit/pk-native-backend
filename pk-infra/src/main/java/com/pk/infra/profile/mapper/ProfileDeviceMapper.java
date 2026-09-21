package com.pk.infra.profile.mapper;
import com.pk.core.profile.ProfileDeviceData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ProfileDeviceMapper {
    boolean existsByUserId(@Param("userId") long userId);
    ProfileDeviceData findByDeviceNo(@Param("deviceNo") String deviceNo);

    ProfileDeviceData findLatestByUserId(@Param("userId") long userId);

    int upsertByDeviceNo(ProfileDeviceData data);
}
