package com.pk.core.profile.sync;

import java.util.List;

public sealed interface ProfileSyncPayload {
    record PersonalProfilePayload(
            String provinceCode,
            String cityCode,
            String districtCode,
            String address,
            int educationDegree,
            String motherSurname
    ) implements ProfileSyncPayload {
    }

    record WorkProfilePayload(
            int industry,
            String companyName,
            String workProvinceCode,
            String workCityCode,
            String workDistrictCode,
            String workAddress,
            String income,
            int payday,
            int professionDegree
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
