package com.pk.core.profile;

public record ProfilePersonalData(
        long profileId,
        String provinceCode,
        String cityCode,
        String districtCode,
        String address,
        int educationDegree,
        EncryptedField motherSurname,
        String userEmail,
        String moduleStatus,
        String lastRequestId
) {
}
