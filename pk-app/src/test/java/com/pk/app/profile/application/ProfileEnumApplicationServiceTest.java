package com.pk.app.profile.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.infra.profile.ApiPartnerLenderEnumMapper;
import com.pk.infra.profile.ApiPartnerProfileEnumCatalog;
import org.junit.jupiter.api.Test;

class ProfileEnumApplicationServiceTest {
    private final ProfileEnumApplicationService service = new ProfileEnumApplicationService(
            new ApiPartnerProfileEnumCatalog(),
            new ApiPartnerLenderEnumMapper(new ApiPartnerProfileEnumCatalog())
    );

    @Test
    void returnsPersonalEnumsWhenModuleFiltered() {
        var response = service.listEnums("personal");

        assertThat(response.lenderProvider()).isEqualTo("apipartner");
        assertThat(response.fields())
                .extracting(field -> field.fieldKey())
                .containsExactly("educationDegree", "industry");
        assertThat(response.fields().getFirst().fieldKey()).isEqualTo("educationDegree");
        assertThat(response.fields().getFirst().lenderField()).isEqualTo("educationDegree");
        assertThat(response.fields().getFirst().options())
                .anyMatch(option -> option.value() == 5 && "S1".equals(option.labelDisplay()));
    }

    @Test
    void returnsAllProfileEnumsWhenModuleOmitted() {
        var response = service.listEnums(null);

        assertThat(response.fields())
                .extracting(field -> field.fieldKey())
                .containsExactly("educationDegree", "industry", "relationship");
    }
}
