package com.pk.core.profile.sync;

import java.util.List;

public sealed interface ProfileSyncPayload {
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
}
