package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.util.regex.Pattern;

public final class MotherSurnameValidator {
    private static final Pattern VALID_FORMAT = Pattern.compile("^[\\p{L}][\\p{L}\\s'.-]*$");

    private MotherSurnameValidator() {
    }

    public static void validate(String motherSurname) {
        if (motherSurname == null || motherSurname.isBlank()) {
            throw new ApiException(ApiCode.MOTHER_SURNAME_REQUIRED);
        }
        if (motherSurname.chars().allMatch(Character::isDigit)) {
            throw new ApiException(ApiCode.INVALID_MOTHER_SURNAME_FORMAT);
        }
        if (!VALID_FORMAT.matcher(motherSurname.trim()).matches()) {
            throw new ApiException(ApiCode.INVALID_MOTHER_SURNAME_FORMAT);
        }
    }
}
