package com.pk.core.profile;

public record ProfileWorkData(
        long profileId,
        int industry,
        String companyName,
        String workProvinceCode,
        String workCityCode,
        String workDistrictCode,
        String workAddress,
        String income,
        int payday,
        int professionDegree,
        String moduleStatus,
        String lastRequestId
) {
}
