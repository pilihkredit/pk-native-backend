package com.pk.infra.debug.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugUserInfoReadMapper {
    UserAccountRecord findAccountByProfileId(@Param("profileId") long profileId);

    IdentityAssetRecord findLatestIdentityAssetByProfileId(@Param("profileId") long profileId);

    List<DeviceRecord> findDevicesByProfileId(@Param("profileId") long profileId);

    record UserAccountRecord(
            long profileId,
            String partnerUserId,
            String externalUserId,
            String mobileNo,
            String email,
            String whatsApp,
            String kycStatus,
            Instant lastSyncedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record IdentityAssetRecord(
            long id,
            String fullName,
            String idCardImageEncryptedRef,
            String facePhotoImageEncryptedRef,
            String ocrChannel,
            String ocrResultJson,
            Instant createdAt
    ) {
    }

    record DeviceRecord(
            String deviceNo,
            String systemPlatform,
            String clientAppName,
            String appVersion,
            String packageName,
            String adId,
            String deviceJson,
            String lastRequestId,
            Instant updatedAt
    ) {
    }
}
