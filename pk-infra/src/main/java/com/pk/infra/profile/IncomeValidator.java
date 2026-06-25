package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.util.regex.Pattern;

public final class IncomeValidator {
    private static final Pattern INCOME_PATTERN = Pattern.compile("^\\d{5,9}$");

    private IncomeValidator() {
    }

    public static void validate(String income) {
        if (income == null || income.isBlank()) {
            throw new ApiException(ApiCode.INCOME_REQUIRED);
        }
        if (!INCOME_PATTERN.matcher(income.trim()).matches()) {
            throw new ApiException(ApiCode.INVALID_INCOME_FORMAT);
        }
    }
}
