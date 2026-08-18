package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import com.pk.core.profile.catalog.ProfileOnboardingModule;
import org.junit.jupiter.api.Test;

class ApiPartnerProfileEnumCatalogTest {
    private final ApiPartnerProfileEnumCatalog catalog = new ApiPartnerProfileEnumCatalog();

    @Test
    void listsPersonalModuleFields() {
        var fields = catalog.listFieldsByModule(ProfileOnboardingModule.PERSONAL);

        assertThat(fields)
                .extracting(field -> field.fieldKey())
                .containsExactly("educationDegree", "industry");
        assertThat(fields.getFirst().options()).hasSize(9);
        assertThat(fields.get(1).options()).hasSize(18);
    }

    @Test
    void validatesEducationDegreeRange() {
        assertThat(catalog.isValid(ProfileEnumFieldKey.EDUCATION_DEGREE, 5)).isTrue();
        assertThat(catalog.isValid(ProfileEnumFieldKey.EDUCATION_DEGREE, 9)).isFalse();
    }

    @Test
    void includesRelationshipValuesFromLenderDoc() {
        var relationship = catalog.findField(ProfileEnumFieldKey.RELATIONSHIP).orElseThrow();

        assertThat(relationship.options())
                .extracting(option -> option.value())
                .containsExactly(0, 1, 10, 11, 12, 13, 14, 15);
    }
}
