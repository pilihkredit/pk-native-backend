package com.pk.app.profile.dto.response;

import java.util.List;

public record ProfileEnumOptionResponse(
        int value,
        String labelDisplay,
        boolean deprecated
) {
}
