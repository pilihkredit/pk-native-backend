package com.pk.core.profile.catalog;

public enum ProfileEnumFieldKey {
    EDUCATION_DEGREE("educationDegree", ProfileOnboardingModule.PERSONAL, "educationDegree"),
    INDUSTRY("industry", ProfileOnboardingModule.WORK, "industry"),
    PROFESSION_DEGREE("professionDegree", ProfileOnboardingModule.WORK, "professionDegree"),
    RELATIONSHIP("relationship", ProfileOnboardingModule.CONTACT, "relationship");

    private final String fieldKey;
    private final ProfileOnboardingModule module;
    private final String lenderField;

    ProfileEnumFieldKey(String fieldKey, ProfileOnboardingModule module, String lenderField) {
        this.fieldKey = fieldKey;
        this.module = module;
        this.lenderField = lenderField;
    }

    public String fieldKey() {
        return fieldKey;
    }

    public ProfileOnboardingModule module() {
        return module;
    }

    public String lenderField() {
        return lenderField;
    }
}
