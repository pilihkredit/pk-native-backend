package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.util.regex.Pattern;

public final class UserEmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private UserEmailValidator() {
    }

    public static void validateOptional(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return;
        }
        if (userEmail.length() > 128 || !EMAIL_PATTERN.matcher(userEmail).matches()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
