package com.pk.core.profile.catalog;

import java.util.List;

public record ProfileEnumFieldDefinition(
        ProfileEnumFieldKey key,
        String valueType,
        List<ProfileEnumOptionDefinition> options
) {
    public String fieldKey() {
        return key.fieldKey();
    }

    public String module() {
        return key.module().apiValue();
    }

    public String lenderField() {
        return key.lenderField();
    }
}
