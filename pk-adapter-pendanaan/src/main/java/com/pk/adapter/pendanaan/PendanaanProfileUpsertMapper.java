package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.math.BigDecimal;

final class PendanaanProfileUpsertMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DEFAULT_MARRIAGE = 0;

    private PendanaanProfileUpsertMapper() {
    }

    static void applyModule(ObjectNode userInfo, ProfileSyncModule module, ProfileSyncPayload payload) {
        switch (module) {
            case PERSONAL -> applyPersonal(userInfo, (ProfileSyncPayload.PersonalProfilePayload) payload);
            case WORK -> applyWork(userInfo, (ProfileSyncPayload.WorkProfilePayload) payload);
            case CONTACT -> applyContact(userInfo, (ProfileSyncPayload.ContactProfilePayload) payload);
            case BANK_CARD -> applyBankCard(userInfo, (ProfileSyncPayload.BankCardProfilePayload) payload);
        }
    }

    private static void applyPersonal(ObjectNode userInfo, ProfileSyncPayload.PersonalProfilePayload payload) {
        ObjectNode profile = userInfo.putObject("profile");
        profile.put("provinceId", parseAreaId(payload.provinceCode()));
        profile.put("cityId", parseAreaId(payload.cityCode()));
        profile.put("districtId", parseAreaId(payload.districtCode()));
        profile.put("address", payload.address());
        profile.put("educationDegree", payload.educationDegree());
        profile.put("marriage", DEFAULT_MARRIAGE);
        profile.put("motherSurname", payload.motherSurname());
    }

    private static void applyWork(ObjectNode userInfo, ProfileSyncPayload.WorkProfilePayload payload) {
        ObjectNode job = userInfo.putObject("job");
        job.put("industry", payload.industry());
        job.put("name", payload.companyName());
        job.put("provinceId", parseAreaId(payload.workProvinceCode()));
        job.put("cityId", parseAreaId(payload.workCityCode()));
        job.put("districtId", parseAreaId(payload.workDistrictCode()));
        job.put("address", payload.workAddress());
        job.put("income", new BigDecimal(payload.income()));
        job.put("payday", payload.payday());
        job.put("professionDegree", payload.professionDegree());
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

    static void applyDevice(ObjectNode userInfo, com.pk.core.profile.sync.LenderDeviceContext device) {
        ObjectNode deviceNode = userInfo.putObject("device");
        deviceNode.put("appName", device.appName());
        deviceNode.put("appVersion", device.appVersion());
        deviceNode.put("packageName", device.packageName());
        deviceNode.put("deviceNo", device.deviceNo());
        deviceNode.put("systemPlatform", device.systemPlatform());
        if (device.adId() != null && !device.adId().isBlank()) {
            deviceNode.put("adId", device.adId());
        }
        if (device.deviceOtherInfo() != null && !device.deviceOtherInfo().isEmpty()) {
            deviceNode.set("deviceOtherInfo", OBJECT_MAPPER.valueToTree(device.deviceOtherInfo()));
        }
    }

    private static int parseAreaId(String areaCode) {
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code is required");
        }
        return Integer.parseInt(areaCode.trim());
    }
}
