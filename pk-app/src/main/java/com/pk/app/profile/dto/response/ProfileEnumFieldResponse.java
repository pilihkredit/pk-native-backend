package com.pk.app.profile.dto.response;

import java.util.List;

public record ProfileEnumFieldResponse(
        String fieldKey,
        String module,
        String lenderField,
        String valueType,
        List<ProfileEnumOptionResponse> options
) {
}
