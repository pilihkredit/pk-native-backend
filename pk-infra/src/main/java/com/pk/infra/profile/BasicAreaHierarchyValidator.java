package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.AreaHierarchyValidator;

public class BasicAreaHierarchyValidator implements AreaHierarchyValidator {
    @Override
    public void validateResidentialHierarchy(String provinceCode, String cityCode, String districtCode) {
        if (isBlank(provinceCode) || isBlank(cityCode) || isBlank(districtCode)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (provinceCode.equals(cityCode) || cityCode.equals(districtCode)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
