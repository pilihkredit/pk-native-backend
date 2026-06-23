package com.pk.app.profile.dto.response;

import java.util.List;

public record ProfileEnumsResponse(
        String lenderProvider,
        List<ProfileEnumFieldResponse> fields
) {
}
