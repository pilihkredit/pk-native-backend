package com.pk.infra.profile;

import com.pk.core.profile.catalog.ProfileEnumFieldDefinition;
import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import com.pk.core.profile.catalog.ProfileEnumOptionDefinition;
import com.pk.core.profile.catalog.ProfileOnboardingModule;
import com.pk.core.profile.port.ProfileEnumCatalog;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Canonical profile enum catalog aligned with Pendanaan OpenAPI v1.1.0.
 */
public class PendanaanProfileEnumCatalog implements ProfileEnumCatalog {
    private static final String VALUE_TYPE_INTEGER = "integer";

    private final List<ProfileEnumFieldDefinition> fields;
    private final Map<ProfileEnumFieldKey, Set<Integer>> validValuesByField;

    public PendanaanProfileEnumCatalog() {
        this.fields = List.of(
                field(ProfileEnumFieldKey.EDUCATION_DEGREE, educationDegreeOptions()),
                field(ProfileEnumFieldKey.INDUSTRY, industryOptions()),
                field(ProfileEnumFieldKey.RELATIONSHIP, relationshipOptions())
        );
        this.validValuesByField = buildValidValues(fields);
    }

    @Override
    public List<ProfileEnumFieldDefinition> listFields() {
        return fields;
    }

    @Override
    public List<ProfileEnumFieldDefinition> listFieldsByModule(ProfileOnboardingModule module) {
        return fields.stream()
                .filter(field -> field.key().module() == module)
                .toList();
    }

    @Override
    public Optional<ProfileEnumFieldDefinition> findField(ProfileEnumFieldKey fieldKey) {
        return fields.stream()
                .filter(field -> field.key() == fieldKey)
                .findFirst();
    }

    @Override
    public boolean isValid(ProfileEnumFieldKey fieldKey, int value) {
        Set<Integer> validValues = validValuesByField.get(fieldKey);
        return validValues != null && validValues.contains(value);
    }

    private static ProfileEnumFieldDefinition field(
            ProfileEnumFieldKey fieldKey,
            List<ProfileEnumOptionDefinition> options
    ) {
        return new ProfileEnumFieldDefinition(fieldKey, VALUE_TYPE_INTEGER, List.copyOf(options));
    }

    private static Map<ProfileEnumFieldKey, Set<Integer>> buildValidValues(List<ProfileEnumFieldDefinition> fields) {
        Map<ProfileEnumFieldKey, Set<Integer>> values = new EnumMap<>(ProfileEnumFieldKey.class);
        for (ProfileEnumFieldDefinition field : fields) {
            values.put(
                    field.key(),
                    field.options().stream()
                            .map(ProfileEnumOptionDefinition::value)
                            .collect(Collectors.toUnmodifiableSet())
            );
        }
        return values;
    }

    private static List<ProfileEnumOptionDefinition> educationDegreeOptions() {
        return List.of(
                option(0, "Legacy compatibility (empty)", "-", true),
                option(1, "No formal education", "Tidak bersekolah", false),
                option(2, "Primary school (SD)", "SD", false),
                option(3, "Junior high school (SMP)", "SMP", false),
                option(4, "Senior high school (SMA)", "SMA", false),
                option(5, "Bachelor's degree (S1)", "S1", false),
                option(6, "Master's degree (S2)", "S2", false),
                option(7, "Doctorate (S3)", "S3", false),
                option(8, "Other (Lainnya)", "Lainnya", false)
        );
    }

    private static List<ProfileEnumOptionDefinition> industryOptions() {
        return List.of(
                option(0, "Finance", "Keuangan", false),
                option(1, "Automotive", "Otomotif", false),
                option(2, "Construction", "Konstruksi", false),
                option(3, "Transportation/UBER", "Transportasi/UBER", false),
                option(4, "Food & beverage", "Makanan & minuman", false),
                option(5, "Security", "Keamanan", false),
                option(6, "Domestic services", "Jasa rumah tangga", false),
                option(7, "Logistics", "Logistik", false),
                option(8, "Military", "Militer", false),
                option(9, "Government", "Pemerintah", false),
                option(10, "Healthcare", "Kesehatan", false),
                option(11, "Manufacturing", "Manufaktur", false),
                option(12, "Wellness", "Kesejahteraan", false),
                option(13, "Mining", "Pertambangan", false),
                option(14, "Transport", "Angkutan", false),
                option(15, "Information technology", "Teknologi informasi", false),
                option(16, "Telecommunications", "Telekomunikasi", false),
                option(99, "Other", "Lainnya", false)
        );
    }

    private static List<ProfileEnumOptionDefinition> relationshipOptions() {
        return List.of(
                option(0, "Mother", "Ibu", false),
                option(1, "Father", "Ayah", false),
                option(10, "Spouse", "Pasangan", false),
                option(11, "Brother", "Saudara laki-laki", false),
                option(12, "Sister", "Saudara perempuan", false),
                option(13, "Partner", "Pacar", false),
                option(14, "Uncle", "Paman", false),
                option(15, "Aunt", "Bibi", false)
        );
    }

    private static ProfileEnumOptionDefinition option(
            int value,
            String labelEn,
            String labelDisplay,
            boolean deprecated
    ) {
        return new ProfileEnumOptionDefinition(value, labelEn, labelDisplay, deprecated);
    }
}
