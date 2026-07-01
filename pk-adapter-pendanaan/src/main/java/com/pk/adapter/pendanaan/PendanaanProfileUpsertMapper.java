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

    static void applyDevice(ObjectNode userInfo, com.pk.core.profile.sync.LenderDeviceContext device) {
        userInfo.set("device", PendanaanDeviceNodeBuilder.buildProfileSyncDevice(device));
    }
}
