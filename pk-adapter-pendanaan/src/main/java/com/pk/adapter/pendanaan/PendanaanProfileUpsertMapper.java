package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;

final class PendanaanProfileUpsertMapper {
    private PendanaanProfileUpsertMapper() {
    }

    static void applyMobileNo(ObjectNode userInfo, String mobileNo) {
        if (mobileNo != null && !mobileNo.isBlank()) {
            userInfo.put("mobileNo", mobileNo.trim());
        }
    }

    static void applyModule(ObjectNode userInfo, ProfileSyncModule module, ProfileSyncPayload payload) {
        switch (module) {
            case PERSONAL -> applyPersonal(userInfo, (ProfileSyncPayload.PersonalProfilePayload) payload);
            case CONTACT -> applyContact(userInfo, (ProfileSyncPayload.ContactProfilePayload) payload);
            case BANK_CARD -> applyBankCard(userInfo, (ProfileSyncPayload.BankCardProfilePayload) payload);
            case IDENTITY -> applyIdentity(userInfo, (ProfileSyncPayload.IdentityProfilePayload) payload);
            case LOGIN_LOG -> applyLoginLog(userInfo, (ProfileSyncPayload.LoginLogProfilePayload) payload);
            case APPSFLYER_INSTALL -> applyAppsFlyer(userInfo, (ProfileSyncPayload.AppsFlyerInstallPayload) payload);
            case TONGDUN_DEVICE -> applyTongdun(userInfo, (ProfileSyncPayload.TongdunDevicePayload) payload);
        }
    }

    private static void applyPersonal(ObjectNode userInfo, ProfileSyncPayload.PersonalProfilePayload payload) {
        ObjectNode profile = userInfo.putObject("profile");
        profile.put("educationDegree", payload.educationDegree());
        profile.put("industry", payload.industry());
        profile.put("income", payload.income());
        profile.put("motherSurname", payload.motherSurname());
        if (payload.userEmail() != null && !payload.userEmail().isBlank()) {
            profile.put("userEmail", payload.userEmail());
        }
    }

    private static void applyContact(ObjectNode userInfo, ProfileSyncPayload.ContactProfilePayload payload) {
        ObjectNode contact = userInfo.putObject("contact");
        var contactsNode = contact.putArray("userContacts");
        for (ProfileSyncPayload.ContactProfilePayload.ContactItem item : payload.contacts()) {
            ObjectNode contactNode = contactsNode.addObject();
            contactNode.put("relationship", item.relationship());
            contactNode.put("mobileNo", item.contactMobile());
            contactNode.put("mobileName", item.contactName());
        }
    }

    private static void applyBankCard(ObjectNode userInfo, ProfileSyncPayload.BankCardProfilePayload payload) {
        ObjectNode bankCard = userInfo.putObject("bankCard");
        bankCard.put("bankCode", payload.bankCode());
        bankCard.put("cardNumber", payload.cardNumber());
    }

    private static void applyIdentity(ObjectNode userInfo, ProfileSyncPayload.IdentityProfilePayload payload) {
        ObjectNode identity = userInfo.putObject("identity");
        identity.put("name", payload.name());
        identity.put("idNo", payload.idNo());
        identity.put("faceBase64", payload.faceBase64());
        identity.put("livenessChannel", payload.ocrChannel());
        if (payload.livenessId() != null && !payload.livenessId().isBlank()) {
            identity.put("livenessId", payload.livenessId());
        }
        identity.put("idCardBase64", payload.idCardBase64());
        ObjectNode ocrResult = identity.putObject("ocrResult");
        putIfPresent(ocrResult, "ocrName", payload.ocrName());
        putIfPresent(ocrResult, "ocrIdNo", payload.ocrIdNo());
        putIfPresent(ocrResult, "gender", payload.gender());
        putIfPresent(ocrResult, "religion", payload.religion());
        putIfPresent(ocrResult, "maritalStatus", payload.maritalStatus());
        putIfPresent(ocrResult, "birthday", payload.birthday());
        putIfPresent(ocrResult, "birthPlace", payload.birthPlace());
        putIfPresent(ocrResult, "address", payload.address());
        putIfPresent(ocrResult, "occupation", payload.occupation());
        putIfPresent(ocrResult, "nationality", payload.nationality());
        putIfPresent(ocrResult, "bloodType", payload.bloodType());
        putIfPresent(ocrResult, "expiryDate", payload.expiryDate());
        ocrResult.put("rawOcrDetail", payload.rawOcrDetail());
        ocrResult.put("ocrChannel", payload.ocrChannel());
    }

    private static void applyLoginLog(ObjectNode userInfo, ProfileSyncPayload.LoginLogProfilePayload payload) {
        ObjectNode loginLog = userInfo.putObject("loginLog");
        loginLog.put("loginType", payload.loginType());
        loginLog.put("loginIp", payload.loginIp());
        if (payload.loginLat() != null) {
            loginLog.put("loginLat", payload.loginLat());
        }
        if (payload.loginLng() != null) {
            loginLog.put("loginLng", payload.loginLng());
        }
    }

    private static void applyAppsFlyer(ObjectNode userInfo, ProfileSyncPayload.AppsFlyerInstallPayload payload) {
        ObjectNode appsFlyer = userInfo.putObject("appsFlyerInstall");
        putIfPresent(appsFlyer, "appsflyerId", payload.appsflyerId());
        putIfPresent(appsFlyer, "advertisingId", payload.advertisingId());
        putIfPresent(appsFlyer, "androidId", payload.androidId());
        putIfPresent(appsFlyer, "attributedTouchTime", payload.attributedTouchTime());
        putIfPresent(appsFlyer, "gpClickTime", payload.gpClickTime());
        putIfPresent(appsFlyer, "installTime", payload.installTime());
        putIfPresent(appsFlyer, "mediaSource", payload.mediaSource());
        putIfPresent(appsFlyer, "afPrt", payload.afPrt());
        putIfPresent(appsFlyer, "afAdsetId", payload.afAdsetId());
        putIfPresent(appsFlyer, "afAdset", payload.afAdset());
        putIfPresent(appsFlyer, "afSiteid", payload.afSiteid());
        putIfPresent(appsFlyer, "afCId", payload.afCId());
        putIfPresent(appsFlyer, "campaign", payload.campaign());
        putIfPresent(appsFlyer, "appVersion", payload.appVersion());
        putIfPresent(appsFlyer, "appId", payload.appId());
        putIfPresent(appsFlyer, "deviceType", payload.deviceType());
        putIfPresent(appsFlyer, "osVersion", payload.osVersion());
        putIfPresent(appsFlyer, "countryCode", payload.countryCode());
        putIfPresent(appsFlyer, "city", payload.city());
        putIfPresent(appsFlyer, "postalCode", payload.postalCode());
        putIfPresent(appsFlyer, "ip", payload.ip());
        putIfPresent(appsFlyer, "operator", payload.operator());
        putIfPresent(appsFlyer, "deviceCategory", payload.deviceCategory());
        putIfPresent(appsFlyer, "platform", payload.platform());
        putIfPresent(appsFlyer, "deviceModel", payload.deviceModel());
        putIfPresent(appsFlyer, "idfv", payload.idfv());
        putIfPresent(appsFlyer, "idfa", payload.idfa());
        putIfPresent(appsFlyer, "afAd", payload.afAd());
        putIfPresent(appsFlyer, "afChannel", payload.afChannel());
        putIfPresent(appsFlyer, "attributedTouchType", payload.attributedTouchType());
        putIfPresent(appsFlyer, "afAdId", payload.afAdId());
        putIfPresent(appsFlyer, "afAdType", payload.afAdType());
        putIfPresent(appsFlyer, "contributor1TouchType", payload.contributor1TouchType());
        putIfPresent(appsFlyer, "contributor1TouchTime", payload.contributor1TouchTime());
        putIfPresent(appsFlyer, "contributor1AfPrt", payload.contributor1AfPrt());
        putIfPresent(appsFlyer, "contributor1MatchType", payload.contributor1MatchType());
        putIfPresent(appsFlyer, "contributor1EngagementType", payload.contributor1EngagementType());
        putIfPresent(appsFlyer, "bundleId", payload.bundleId());
        putIfPresent(appsFlyer, "matchType", payload.matchType());
        putIfPresent(appsFlyer, "gpInstallBegin", payload.gpInstallBegin());
    }

    private static void applyTongdun(ObjectNode userInfo, ProfileSyncPayload.TongdunDevicePayload payload) {
        ObjectNode tongdun = userInfo.putObject("tongdunDevice");
        tongdun.put("sceneType", payload.sceneType());
        tongdun.put("tongdunKey", payload.tongdunKey());
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }

    static void applyDevice(ObjectNode userInfo, com.pk.core.profile.sync.LenderDeviceContext device) {
        userInfo.set("device", PendanaanDeviceNodeBuilder.buildProfileSyncDevice(device));
    }
}
