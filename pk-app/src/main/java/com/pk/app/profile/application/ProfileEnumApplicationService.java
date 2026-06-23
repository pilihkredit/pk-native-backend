package com.pk.app.profile.application;

import com.pk.app.profile.dto.response.ProfileEnumFieldResponse;
import com.pk.app.profile.dto.response.ProfileEnumOptionResponse;
import com.pk.app.profile.dto.response.ProfileEnumsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.catalog.ProfileEnumFieldDefinition;
import com.pk.core.profile.catalog.ProfileEnumOptionDefinition;
import com.pk.core.profile.catalog.ProfileOnboardingModule;
import com.pk.core.profile.port.LenderEnumMapper;
import com.pk.core.profile.port.ProfileEnumCatalog;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileEnumApplicationService {
    private final ProfileEnumCatalog profileEnumCatalog;
    private final LenderEnumMapper lenderEnumMapper;

    public ProfileEnumApplicationService(
            ProfileEnumCatalog profileEnumCatalog,
            LenderEnumMapper lenderEnumMapper
    ) {
        this.profileEnumCatalog = profileEnumCatalog;
        this.lenderEnumMapper = lenderEnumMapper;
    }

    public ProfileEnumsResponse listEnums(String module) {
        List<ProfileEnumFieldDefinition> fields = resolveFields(module);
        return new ProfileEnumsResponse(
                lenderEnumMapper.providerCode(),
                fields.stream().map(this::toFieldResponse).toList()
        );
    }

    private List<ProfileEnumFieldDefinition> resolveFields(String module) {
        if (module == null || module.isBlank()) {
            return profileEnumCatalog.listFields();
        }
        try {
            return profileEnumCatalog.listFieldsByModule(ProfileOnboardingModule.fromApiValue(module));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private ProfileEnumFieldResponse toFieldResponse(ProfileEnumFieldDefinition field) {
        return new ProfileEnumFieldResponse(
                field.fieldKey(),
                field.module(),
                field.lenderField(),
                field.valueType(),
                field.options().stream().map(this::toOptionResponse).toList()
        );
    }

    private ProfileEnumOptionResponse toOptionResponse(ProfileEnumOptionDefinition option) {
        return new ProfileEnumOptionResponse(
                option.value(),
                option.labelDisplay(),
                option.deprecated()
        );
    }
}
