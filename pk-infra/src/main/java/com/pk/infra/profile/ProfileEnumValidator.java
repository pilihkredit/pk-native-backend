package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import com.pk.core.profile.port.ProfileEnumCatalog;

public final class ProfileEnumValidator {
    private final ProfileEnumCatalog profileEnumCatalog;

    public ProfileEnumValidator(ProfileEnumCatalog profileEnumCatalog) {
        this.profileEnumCatalog = profileEnumCatalog;
    }

    public void validateEducationDegree(Integer educationDegree) {
        if (educationDegree == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!profileEnumCatalog.isValid(ProfileEnumFieldKey.EDUCATION_DEGREE, educationDegree)) {
            throw new ApiException(ApiCode.INVALID_EDUCATION_DEGREE);
        }
    }

    public void validateIndustry(Integer industry) {
        if (industry == null) {
            throw new ApiException(ApiCode.INDUSTRY_REQUIRED);
        }
        if (!profileEnumCatalog.isValid(ProfileEnumFieldKey.INDUSTRY, industry)) {
            throw new ApiException(ApiCode.INDUSTRY_REQUIRED);
        }
    }

    public void validateRelationship(Integer relationship) {
        if (relationship == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!profileEnumCatalog.isValid(ProfileEnumFieldKey.RELATIONSHIP, relationship)) {
            throw new ApiException(ApiCode.INVALID_CONTACT_RELATIONSHIP);
        }
    }
}
