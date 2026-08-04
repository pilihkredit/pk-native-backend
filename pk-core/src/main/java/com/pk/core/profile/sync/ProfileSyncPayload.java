package com.pk.core.profile.sync;

import java.util.List;

public sealed interface ProfileSyncPayload {
    record MobilePayload() implements ProfileSyncPayload {
    }

    record PersonalProfilePayload(
            int educationDegree,
            int industry,
            String income,
            String motherSurname,
            String userEmail
    ) implements ProfileSyncPayload {
    }

    record ContactProfilePayload(
            List<ContactItem> contacts
    ) implements ProfileSyncPayload {
        public record ContactItem(
                int relationship,
                String contactName,
                String contactMobile
        ) {
        }
    }

    record BankCardProfilePayload(
            String bankCode,
            String cardNumber
    ) implements ProfileSyncPayload {
    }

    record IdentityProfilePayload(
            String name,
            String idNo,
            String faceBase64,
            String livenessId,
            String idCardBase64,
            String rawOcrDetail,
            String ocrChannel,
            String ocrName,
            String ocrIdNo,
            String gender,
            String religion,
            String maritalStatus,
            String birthday,
            String birthPlace,
            String address,
            String occupation,
            String nationality,
            String bloodType,
            String expiryDate
    ) implements ProfileSyncPayload {
    }

    record LoginLogProfilePayload(
            int loginType,
            String loginIp,
            java.math.BigDecimal loginLat,
            java.math.BigDecimal loginLng
    ) implements ProfileSyncPayload {
    }

    record AppsFlyerInstallPayload(
            String appsflyerId,
            String advertisingId,
            String androidId,
            String attributedTouchTime,
            String gpClickTime,
            String installTime,
            String mediaSource,
            String afPrt,
            String afAdsetId,
            String afAdset,
            String afSiteid,
            String afCId,
            String campaign,
            String appVersion,
            String appId,
            String deviceType,
            String osVersion,
            String countryCode,
            String city,
            String postalCode,
            String ip,
            String operator,
            String deviceCategory,
            String platform,
            String deviceModel,
            String idfv,
            String idfa,
            String afAd,
            String afChannel,
            String attributedTouchType,
            String afAdId,
            String afAdType,
            String contributor1TouchType,
            String contributor1TouchTime,
            String contributor1AfPrt,
            String contributor1MatchType,
            String contributor1EngagementType,
            String bundleId,
            String matchType,
            String gpInstallBegin
    ) implements ProfileSyncPayload {
    }

    record TongdunDevicePayload(
            String sceneType,
            String tongdunKey
    ) implements ProfileSyncPayload {
    }
}
