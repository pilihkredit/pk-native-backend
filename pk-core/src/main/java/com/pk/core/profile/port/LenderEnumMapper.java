package com.pk.core.profile.port;

import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import java.util.OptionalInt;

/**
 * Maps canonical app enum values to lender-specific values during profile sync.
 * Pendanaan uses the same integer codes today; future lenders may differ.
 */
public interface LenderEnumMapper {
    String providerCode();

    String toLenderValue(ProfileEnumFieldKey fieldKey, int appValue);

    OptionalInt fromLenderValue(ProfileEnumFieldKey fieldKey, String lenderValue);
}
