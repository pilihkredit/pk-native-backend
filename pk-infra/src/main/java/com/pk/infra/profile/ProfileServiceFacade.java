package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.infra.auth.MobileNumberValidator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileServiceFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    private static final int MIN_CONTACT_COUNT = 2;

    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final AreaHierarchyValidator areaHierarchyValidator;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileEnumValidator profileEnumValidator;

    public ProfileServiceFacade(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            AreaHierarchyValidator areaHierarchyValidator,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
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

    public ContactsSaveResult saveContacts(long profileId, String userMobileNo, ContactsSaveCommand command) {
        validateContacts(command, userMobileNo);

        var existing = profileContactRepository.findModuleByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new ContactsSaveResult(command.requestId(), MODULE_COMPLETED);
        }

        List<ProfileContactData> contacts = toContactData(command.contacts());
        profileContactRepository.replaceContacts(
                profileId,
                new ProfileContactsModuleData(profileId, MODULE_COMPLETED, command.requestId()),
                contacts
        );

        return new ContactsSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    private void validateContacts(ContactsSaveCommand command, String userMobileNo) {
        if (command.requestId() == null || command.requestId().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.contacts() == null || command.contacts().size() < MIN_CONTACT_COUNT) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        Set<String> seenMobiles = new HashSet<>();
        String normalizedUserMobile = normalizeMobile(userMobileNo);

        for (ContactItemCommand contact : command.contacts()) {
            validateContactItem(contact, normalizedUserMobile, seenMobiles);
        }
    }

    private void validateContactItem(
            ContactItemCommand contact,
            String normalizedUserMobile,
            Set<String> seenMobiles
    ) {
        profileEnumValidator.validateRelationship(contact.relationship());

        if (contact.contactName() == null || contact.contactName().isBlank()) {
            throw new ApiException(ApiCode.CONTACT_NAME_REQUIRED);
        }
        if (contact.contactName().trim().length() > 128) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        if (contact.contactMobile() == null || contact.contactMobile().isBlank()) {
            throw new ApiException(ApiCode.CONTACT_MOBILE_REQUIRED);
        }
        String normalizedMobile = normalizeMobile(contact.contactMobile());
        if (!MobileNumberValidator.isValid(normalizedMobile)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        if (normalizedMobile.equals(normalizedUserMobile)) {
            throw new ApiException(ApiCode.CONTACT_MOBILE_SAME_AS_OWN);
        }
        if (!seenMobiles.add(normalizedMobile)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static List<ProfileContactData> toContactData(List<ContactItemCommand> contacts) {
        var result = new java.util.ArrayList<ProfileContactData>(contacts.size());
        for (int index = 0; index < contacts.size(); index++) {
            ContactItemCommand contact = contacts.get(index);
            result.add(new ProfileContactData(
                    index + 1,
                    contact.relationship(),
                    contact.contactName().trim(),
                    normalizeMobile(contact.contactMobile())
            ));
        }
        return List.copyOf(result);
    }

    private static String normalizeMobile(String mobileNo) {
        return mobileNo == null ? "" : mobileNo.trim();
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

    public record ContactItemCommand(
            Integer relationship,
            String contactName,
            String contactMobile
    ) {
    }

    public record ContactsSaveCommand(
            String requestId,
            List<ContactItemCommand> contacts
    ) {
    }

    public record ContactsSaveResult(String requestId, String moduleStatus) {
    }
}
