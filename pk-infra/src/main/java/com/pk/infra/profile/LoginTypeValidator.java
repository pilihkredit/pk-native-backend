package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.util.Set;

final class LoginTypeValidator {
    private static final Set<Integer> ALLOWED_LOGIN_TYPES = Set.of(1, 2, 4, 5);

    private LoginTypeValidator() {
    }

    static void validate(Integer loginType) {
        if (loginType == null || !ALLOWED_LOGIN_TYPES.contains(loginType)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
