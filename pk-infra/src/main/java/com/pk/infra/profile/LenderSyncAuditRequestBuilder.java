package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class LenderSyncAuditRequestBuilder {
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ObjectMapper objectMapper;

    public LenderSyncAuditRequestBuilder(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ObjectMapper objectMapper
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
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
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("requestId", requestId);
            root.put("partnerUserId", partnerUserId);
            ObjectNode userInfo = objectMapper.createObjectNode();
            if (mobileNo != null && !mobileNo.isBlank()) {
                userInfo.put("mobileNo", mobileNo.trim());
            }
            applyModuleAudit(userInfo, module, payload, profileId);
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
            long profileId
    ) {
        switch (module) {
            case PERSONAL -> applyPersonalAudit(userInfo, profileId);
            case CONTACT -> applyContactAudit(userInfo, profileId);
            case BANK_CARD -> applyBankCardAudit(userInfo, profileId);
            case IDENTITY -> applyIdentityAudit(userInfo, (ProfileSyncPayload.IdentityProfilePayload) payload);
        }
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

    private void applyBankCardAudit(ObjectNode userInfo, long profileId) {
        ProfileBankCardData data = profileBankCardRepository.findByProfileId(profileId)
                .orElseThrow(() -> new IllegalStateException("bank card module data is missing"));
        ObjectNode bankCard = userInfo.putObject("bankCard");
        bankCard.put("bankCode", data.bankCode());
        bankCard.set("cardNumber", toEncryptedJsonNode(data.cardNumber()));
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
