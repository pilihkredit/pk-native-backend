package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import org.junit.jupiter.api.Test;

class PendanaanLenderEnumMapperTest {
    private final PendanaanLenderEnumMapper mapper =
            new PendanaanLenderEnumMapper(new PendanaanProfileEnumCatalog());

    @Test
    void mapsValidValuesOneToOneForPendanaan() {
        assertThat(mapper.providerCode()).isEqualTo("pendanaan");
        assertThat(mapper.toLenderValue(ProfileEnumFieldKey.EDUCATION_DEGREE, 5)).isEqualTo("5");
        assertThat(mapper.fromLenderValue(ProfileEnumFieldKey.INDUSTRY, "99"))
                .hasValue(99);
    }
}
