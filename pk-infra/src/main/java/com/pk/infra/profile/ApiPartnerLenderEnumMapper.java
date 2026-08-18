package com.pk.infra.profile;

import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import com.pk.core.profile.port.LenderEnumMapper;
import com.pk.core.profile.port.ProfileEnumCatalog;
import java.util.OptionalInt;

/**
 * ApiPartner OpenAPI uses the same integer enum codes as the app catalog today.
 */
public class ApiPartnerLenderEnumMapper implements LenderEnumMapper {
    public static final String PROVIDER_CODE = "apipartner";

    private final ProfileEnumCatalog profileEnumCatalog;

    public ApiPartnerLenderEnumMapper(ProfileEnumCatalog profileEnumCatalog) {
        this.profileEnumCatalog = profileEnumCatalog;
    }

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String toLenderValue(ProfileEnumFieldKey fieldKey, int appValue) {
        if (!profileEnumCatalog.isValid(fieldKey, appValue)) {
            throw new IllegalArgumentException("Unsupported enum value for " + fieldKey.fieldKey() + ": " + appValue);
        }
        return Integer.toString(appValue);
    }

    @Override
    public OptionalInt fromLenderValue(ProfileEnumFieldKey fieldKey, String lenderValue) {
        if (lenderValue == null || lenderValue.isBlank()) {
            return OptionalInt.empty();
        }
        try {
            int value = Integer.parseInt(lenderValue.trim());
            if (!profileEnumCatalog.isValid(fieldKey, value)) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(value);
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }
}
