package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;

public final class PaydayValidator {
    private static final int MIN_PAYDAY = 1;
    private static final int MAX_PAYDAY = 31;

    private PaydayValidator() {
    }

    public static void validate(Integer payday) {
        if (payday == null || payday < MIN_PAYDAY || payday > MAX_PAYDAY) {
            throw new ApiException(ApiCode.INVALID_PAYDAY);
        }
    }
}
