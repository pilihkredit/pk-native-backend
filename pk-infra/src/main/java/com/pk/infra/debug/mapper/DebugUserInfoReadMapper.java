package com.pk.infra.debug.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugUserInfoReadMapper {
    UserAccountRecord findAccountByUserId(@Param("userId") long userId);

    IdentityAssetRecord findLatestIdentityAssetByUserId(@Param("userId") long userId);

    List<DeviceRecord> findDevicesByUserId(@Param("userId") long userId);

    record UserAccountRecord(
            long userId,
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
            Long ocrVendorCallLogId,
            Long externalInteractionId,
            Instant createdAt
    ) {
    }

    record DeviceRecord(
            String deviceNo,
            String systemPlatform,
            String appName,
            String appVersion,
            String packageName,
            String phoneBrand,
            String phoneBrandModel,
            String mac,
            String systemVersion,
            String deliveryPlatform,
            Integer cpuCores,
            Long memoryTotal,
            Long sdCardTotal,
            String adId,
            String idfv,
            String idfa,
            String extParam,
            String ip,
            String deviceJson,
            String lastRequestId,
            Instant updatedAt
    ) {
    }
}
