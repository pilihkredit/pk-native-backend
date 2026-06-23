package com.pk.core.profile.catalog;

public record ProfileEnumOptionDefinition(
        int value,
        String labelEn,
        String labelDisplay,
        boolean deprecated
) {
}
