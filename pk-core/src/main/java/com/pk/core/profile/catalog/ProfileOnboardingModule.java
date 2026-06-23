package com.pk.core.profile.catalog;

public enum ProfileOnboardingModule {
    PERSONAL("personal"),
    WORK("work"),
    CONTACT("contact");

    private final String apiValue;

    ProfileOnboardingModule(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static ProfileOnboardingModule fromApiValue(String apiValue) {
        if (apiValue == null || apiValue.isBlank()) {
            return null;
        }
        for (ProfileOnboardingModule module : values()) {
            if (module.apiValue.equalsIgnoreCase(apiValue.trim())) {
                return module;
            }
        }
        throw new IllegalArgumentException("Unknown profile module: " + apiValue);
    }
}
