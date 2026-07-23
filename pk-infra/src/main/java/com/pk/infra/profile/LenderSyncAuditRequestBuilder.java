package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileLoginLogData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class LenderSyncAuditRequestBuilder {
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileLoginLogRepository profileLoginLogRepository;
    private final ObjectMapper objectMapper;

    public LenderSyncAuditRequestBuilder(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileLoginLogRepository profileLoginLogRepository,
            ObjectMapper objectMapper
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileLoginLogRepository = profileLoginLogRepository;
        this.objectMapper = objectMapper;
    }

    public String buildProfileUpsertAudit(
            String requestId,
            String partnerUserId,
            String mobileNo,
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            LenderDeviceContext device,
            long profileId
    ) {
        return buildProfileUpsertAudit(requestId, partnerUserId, mobileNo, module, payload, device, profileId, List.of());
    }

    public String buildProfileUpsertAudit(
            String requestId,
            String partnerUserId,
            String mobileNo,
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            LenderDeviceContext device,
            long profileId,
            List<com.pk.core.profile.port.LenderProfileSyncPort.SyncCompanion> companions
    ) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("requestId", requestId);
            root.put("partnerUserId", partnerUserId);
            ObjectNode userInfo = objectMapper.createObjectNode();
            if (mobileNo != null && !mobileNo.isBlank()) {
                userInfo.put("mobileNo", mobileNo.trim());
            }
            applyModuleAudit(userInfo, module, payload, profileId, requestId);
            if (companions != null) {
                for (com.pk.core.profile.port.LenderProfileSyncPort.SyncCompanion companion : companions) {
                    applyModuleAudit(userInfo, companion.module(), companion.payload(), profileId, requestId);
                }
            }
            userInfo.set("device", buildLenderDeviceNode(device));
            root.set("userInfo", userInfo);
            return objectMapper.writeValueAsString(root);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build lender audit request JSON", exception);
        }
    }

    private void applyModuleAudit(
            ObjectNode userInfo,
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            long profileId,
            String requestId
    ) {
        switch (module) {
            case PERSONAL -> applyPersonalAudit(userInfo, profileId);
            case CONTACT -> applyContactAudit(userInfo, profileId);
            case BANK_CARD -> applyBankCardAudit(userInfo, profileId, requestId);
            case IDENTITY -> applyIdentityAudit(userInfo, (ProfileSyncPayload.IdentityProfilePayload) payload);
            case LOGIN_LOG -> applyLoginLogAudit(userInfo, profileId);
            case APPSFLYER_INSTALL -> applyAppsFlyerAudit(
                    userInfo,
                    (ProfileSyncPayload.AppsFlyerInstallPayload) payload
            );
            case TONGDUN_DEVICE -> applyTongdunAudit(
                    userInfo,
                    (ProfileSyncPayload.TongdunDevicePayload) payload
            );
        }
    }

    private void applyAppsFlyerAudit(ObjectNode userInfo, ProfileSyncPayload.AppsFlyerInstallPayload payload) {
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

    private void applyTongdunAudit(ObjectNode userInfo, ProfileSyncPayload.TongdunDevicePayload payload) {
        ObjectNode tongdun = userInfo.putObject("tongdunDevice");
        tongdun.put("sceneType", payload.sceneType());
        tongdun.put("tongdunKey", payload.tongdunKey());
    }

    private void applyPersonalAudit(ObjectNode userInfo, long profileId) {
        ProfilePersonalData data = profilePersonalRepository.findByProfileId(profileId)
                .orElseThrow(() -> new IllegalStateException("personal module data is missing"));
        ObjectNode profile = userInfo.putObject("profile");
        profile.put("educationDegree", data.educationDegree());
        profile.put("industry", data.industry());
        profile.put("income", data.income());
        profile.set("motherSurname", toEncryptedJsonNode(data.motherSurname()));
        if (data.userEmail() != null && !data.userEmail().isBlank()) {
            profile.put("userEmail", data.userEmail());
        }
    }

    private void applyContactAudit(ObjectNode userInfo, long profileId) {
        List<ProfileContactData> contacts = profileContactRepository.findContactsByProfileId(profileId);
        ObjectNode contact = userInfo.putObject("contact");
        ArrayNode contactsNode = contact.putArray("userContacts");
        for (ProfileContactData item : contacts) {
            ObjectNode contactNode = contactsNode.addObject();
            contactNode.put("relationship", item.relationship());
            contactNode.put("mobileNo", item.contactMobile());
            contactNode.put("mobileName", item.contactName());
        }
    }

    private void applyBankCardAudit(ObjectNode userInfo, long profileId, String requestId) {
        ProfileBankCardData data = profileBankCardRepository.findByLastRequestId(requestId)
                .or(() -> profileBankCardRepository.findDefaultByProfileId(profileId))
                .orElseThrow(() -> new IllegalStateException("bank card module data is missing"));
        ObjectNode bankCard = userInfo.putObject("bankCard");
        bankCard.put("bankCode", data.bankCode());
        bankCard.set("cardNumber", toEncryptedJsonNode(data.cardNumber()));
    }

    private void applyLoginLogAudit(ObjectNode userInfo, long profileId) {
        ProfileLoginLogData data = profileLoginLogRepository.findByProfileId(profileId)
                .orElseThrow(() -> new IllegalStateException("login log module data is missing"));
        ObjectNode loginLog = userInfo.putObject("loginLog");
        loginLog.put("loginType", data.loginType());
        loginLog.put("loginIp", data.loginIp());
        if (data.loginLat() != null) {
            loginLog.put("loginLat", data.loginLat());
        }
        if (data.loginLng() != null) {
            loginLog.put("loginLng", data.loginLng());
        }
    }

    private void applyIdentityAudit(ObjectNode userInfo, ProfileSyncPayload.IdentityProfilePayload payload) {
        ObjectNode identity = userInfo.putObject("identity");
        identity.put("name", payload.name());
        identity.put("idNo", payload.idNo());
        identity.put("faceBase64", "[redacted]");
        identity.put("idCardBase64", "[redacted]");
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
        ocrResult.put("rawOcrDetail", "[redacted]");
        ocrResult.put("ocrChannel", payload.ocrChannel());
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }

    private ObjectNode toEncryptedJsonNode(EncryptedField field) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("ciphertext", field.ciphertextBase64());
        node.put("nonce", Base64.getEncoder().encodeToString(field.nonce()));
        node.put("tag", Base64.getEncoder().encodeToString(field.tag()));
        return node;
    }

    private ObjectNode buildLenderDeviceNode(LenderDeviceContext device) {
        ObjectNode deviceNode = objectMapper.createObjectNode();
        deviceNode.put("appName", device.appName());
        deviceNode.put("appVersion", device.appVersion());
        deviceNode.put("packageName", device.packageName());
        deviceNode.put("deviceNo", device.deviceNo());
        deviceNode.put("systemPlatform", device.systemPlatform());
        applyExtendedAttributes(deviceNode, device.resolvedExtendedAttributes());
        putIfPresent(deviceNode, "adId", device.adId());
        putIfPresent(deviceNode, "adChannel", device.resolvedExtendedAttributes().adChannel());
        deviceNode.put("ip", LenderDevicePayloadBuilder.FIXED_CLIENT_IP);
        applyDeviceOtherInfo(deviceNode, device.deviceOtherInfo());
        return deviceNode;
    }

    private static void applyExtendedAttributes(ObjectNode deviceNode, DeviceExtendedAttributes attributes) {
        putIfPresent(deviceNode, "phoneBrand", attributes.phoneBrand());
        putIfPresent(deviceNode, "phoneBrandModel", attributes.phoneBrandModel());
        putIfPresent(deviceNode, "mac", attributes.mac());
        putIfPresent(deviceNode, "systemVersion", attributes.systemVersion());
        putIfPresent(deviceNode, "deliveryPlatform", attributes.deliveryPlatform());
        putIfPresent(deviceNode, "cpuCores", attributes.cpuCores());
        putIfPresent(deviceNode, "memoryTotal", attributes.memoryTotal());
        putIfPresent(deviceNode, "sdCardTotal", attributes.sdCardTotal());
        putIfPresent(deviceNode, "idfv", attributes.idfv());
        putIfPresent(deviceNode, "idfa", attributes.idfa());
        putIfPresent(deviceNode, "extParam", attributes.extParam());
    }

    private void applyDeviceOtherInfo(ObjectNode deviceNode, Map<String, Object> deviceOtherInfo) {
        if (deviceOtherInfo == null || deviceOtherInfo.isEmpty()) {
            return;
        }
        deviceNode.set("deviceOtherInfo", objectMapper.valueToTree(deviceOtherInfo));
    }

    private static void putIfPresent(ObjectNode node, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String stringValue) {
            if (stringValue.isBlank()) {
                return;
            }
            node.put(key, stringValue);
            return;
        }
        if (value instanceof Integer intValue) {
            node.put(key, intValue);
            return;
        }
        if (value instanceof Long longValue) {
            node.put(key, longValue);
            return;
        }
        node.putPOJO(key, value);
    }
}
