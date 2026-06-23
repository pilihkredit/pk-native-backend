package com.pk.core.profile.port;

import com.pk.core.profile.catalog.ProfileEnumFieldDefinition;
import com.pk.core.profile.catalog.ProfileEnumFieldKey;
import com.pk.core.profile.catalog.ProfileOnboardingModule;
import java.util.List;
import java.util.Optional;

public interface ProfileEnumCatalog {
    List<ProfileEnumFieldDefinition> listFields();

    List<ProfileEnumFieldDefinition> listFieldsByModule(ProfileOnboardingModule module);

    Optional<ProfileEnumFieldDefinition> findField(ProfileEnumFieldKey fieldKey);

    boolean isValid(ProfileEnumFieldKey fieldKey, int value);
}
