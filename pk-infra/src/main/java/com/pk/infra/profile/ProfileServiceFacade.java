package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;

public class ProfileServiceFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";

    private final ProfilePersonalRepository profilePersonalRepository;
    private final AreaHierarchyValidator areaHierarchyValidator;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileEnumValidator profileEnumValidator;

    public ProfileServiceFacade(
            ProfilePersonalRepository profilePersonalRepository,
            AreaHierarchyValidator areaHierarchyValidator,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.areaHierarchyValidator = areaHierarchyValidator;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.profileEnumValidator = profileEnumValidator;
    }

    public PersonalSaveResult savePersonal(long profileId, PersonalSaveCommand command) {
        validate(command);

        var existing = profilePersonalRepository.findByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new PersonalSaveResult(command.requestId(), MODULE_COMPLETED);
        }

        EncryptedField motherSurname = sensitiveFieldEncryptor.encrypt(command.motherSurname().trim());
        String normalizedEmail = normalizeEmail(command.userEmail());

        profilePersonalRepository.upsert(new ProfilePersonalData(
                profileId,
                command.provinceCode().trim(),
                command.cityCode().trim(),
                command.districtCode().trim(),
                command.address().trim(),
                command.educationDegree(),
                motherSurname,
                normalizedEmail,
                MODULE_COMPLETED,
                command.requestId()
        ));

        if (normalizedEmail != null) {
            profilePersonalRepository.updateEmail(profileId, normalizedEmail);
        }

        return new PersonalSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    private void validate(PersonalSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.address() == null || command.address().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        areaHierarchyValidator.validateResidentialHierarchy(
                command.provinceCode(),
                command.cityCode(),
                command.districtCode()
        );
        profileEnumValidator.validateEducationDegree(command.educationDegree());
        MotherSurnameValidator.validate(command.motherSurname());
        UserEmailValidator.validateOptional(command.userEmail());
    }

    private static String normalizeEmail(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return null;
        }
        return userEmail.trim();
    }

    public record PersonalSaveCommand(
            String requestId,
            String provinceCode,
            String cityCode,
            String districtCode,
            String address,
            Integer educationDegree,
            String motherSurname,
            String userEmail
    ) {
    }

    public record PersonalSaveResult(String requestId, String moduleStatus) {
    }
}
