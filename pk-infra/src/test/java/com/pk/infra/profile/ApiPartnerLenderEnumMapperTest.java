package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import org.junit.jupiter.api.Test;

class ApiPartnerLenderEnumMapperTest {
    private final ApiPartnerLenderEnumMapper mapper =
            new ApiPartnerLenderEnumMapper(new ApiPartnerProfileEnumCatalog());

    @Test
    void mapsValidValuesOneToOneForApiPartner() {
        assertThat(mapper.providerCode()).isEqualTo("apipartner");
        assertThat(mapper.toLenderValue(ProfileEnumFieldKey.EDUCATION_DEGREE, 5)).isEqualTo("5");
        assertThat(mapper.fromLenderValue(ProfileEnumFieldKey.INDUSTRY, "99"))
                .hasValue(99);
    }
}
