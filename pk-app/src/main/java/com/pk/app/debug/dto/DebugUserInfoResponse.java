package com.pk.app.debug.dto;

import java.time.Instant;
import java.util.List;

public record DebugUserInfoResponse(
        boolean found,
        AccountInfo account,
        ProgressInfo progress,
        IdentityInfo identity,
        PersonalInfo personal,
        ContactsInfo contacts,
        BankCardInfo bankCard,
        List<DeviceInfo> devices,
        OcrSessionInfo ocrSession
) {
    public static DebugUserInfoResponse notFound() {
        return new DebugUserInfoResponse(false, null, null, null, null, null, null, List.of(), null);
    }

    public record AccountInfo(
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

    public record ProgressInfo(
            String kycStatus,
            List<String> completedModules,
            List<String> missingModules
    ) {
    }

    public record IdentityInfo(
            String moduleStatus,
            String lastRequestId,
            String fullName,
            String idNo,
            String idNoHash,
            String gender,
            String religion,
            String maritalStatus,
            String birthday,
            String birthPlace,
            String address,
            String occupation,
            String nationality,
            String bloodType,
            String expiryDate,
            String province,
            String city,
            String district,
            String ocrChannel,
            String ocrResultJson,
            String idCardImageDataUrl,
            String facePhotoDataUrl,
            Instant assetCreatedAt
    ) {
    }

    public record PersonalInfo(
            String moduleStatus,
            String lastRequestId,
            Integer educationDegree,
            Integer industry,
            String income,
            String motherSurname,
            String userEmail
    ) {
    }

    public record ContactItem(
            int sortNo,
            int relationship,
            String contactName,
            String contactMobile
    ) {
    }

    public record ContactsInfo(
            String moduleStatus,
            String lastRequestId,
            List<ContactItem> items
    ) {
    }

    public record BankCardInfo(
            String moduleStatus,
            String lastRequestId,
            String bankCode,
            String cardNumber,
            String cardNoHash,
            String verifyStatus,
            String verifyErrorCode
    ) {
    }

    public record DeviceInfo(
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
            String deviceJson,
            String lastRequestId,
            Instant updatedAt
    ) {
    }

    public record OcrSessionInfo(
            boolean licenseObtained,
            boolean ocrCheckCompleted,
            boolean livenessPassed,
            Integer livenessScore,
            String ocrRawJson,
            Instant updatedAt
    ) {
    }
}
