package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ProfileWorkData;
import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileWorkRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileServiceFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    private static final int MIN_CONTACT_COUNT = 2;

    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileWorkRepository profileWorkRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileDeviceRepository profileDeviceRepository;
    private final AreaHierarchyValidator areaHierarchyValidator;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileEnumValidator profileEnumValidator;
    private final BankReferenceFacade bankReferenceFacade;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;

    public ProfileServiceFacade(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileWorkRepository profileWorkRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileDeviceRepository profileDeviceRepository,
            AreaHierarchyValidator areaHierarchyValidator,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator,
            BankReferenceFacade bankReferenceFacade,
            ProfileSyncOrchestrator profileSyncOrchestrator
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileWorkRepository = profileWorkRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileDeviceRepository = profileDeviceRepository;
        this.areaHierarchyValidator = areaHierarchyValidator;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.profileEnumValidator = profileEnumValidator;
        this.bankReferenceFacade = bankReferenceFacade;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
    }

    public PersonalSaveResult savePersonal(long profileId, String partnerUserId, PersonalSaveCommand command) {
        validate(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

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

        persistLatestDevice(profileId, command.requestId(), command.device());

        if (normalizedEmail != null) {
            profilePersonalRepository.updateEmail(profileId, normalizedEmail);
        }

        profileSyncOrchestrator.scheduleAfterSave(ProfileSyncJob.fromStoredModule(
                profileId,
                partnerUserId,
                command.requestId(),
                ProfileSyncModule.PERSONAL,
                command.device()
        ));

        return new PersonalSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    public WorkSaveResult saveWork(long profileId, String partnerUserId, WorkSaveCommand command) {
        validateWork(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = profileWorkRepository.findByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new WorkSaveResult(command.requestId(), MODULE_COMPLETED);
        }

        profileWorkRepository.upsert(new ProfileWorkData(
                profileId,
                command.industry(),
                command.companyName().trim(),
                command.workProvinceCode().trim(),
                command.workCityCode().trim(),
                command.workDistrictCode().trim(),
                command.workAddress().trim(),
                command.income().trim(),
                command.payday(),
                command.professionDegree(),
                MODULE_COMPLETED,
                command.requestId()
        ));

        persistLatestDevice(profileId, command.requestId(), command.device());

        profileSyncOrchestrator.scheduleAfterSave(ProfileSyncJob.fromStoredModule(
                profileId,
                partnerUserId,
                command.requestId(),
                ProfileSyncModule.WORK,
                command.device()
        ));

        return new WorkSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    public ContactsSaveResult saveContacts(
            long profileId,
            String partnerUserId,
            String userMobileNo,
            ContactsSaveCommand command
    ) {
        validateContacts(command, userMobileNo);
        ProfileSyncPayloadLoader.validateDevice(command.device());

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

        persistLatestDevice(profileId, command.requestId(), command.device());

        profileSyncOrchestrator.scheduleAfterSave(ProfileSyncJob.fromStoredModule(
                profileId,
                partnerUserId,
                command.requestId(),
                ProfileSyncModule.CONTACT,
                command.device()
        ));

        return new ContactsSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    public BankCardSaveResult saveBankCard(long profileId, String partnerUserId, BankCardSaveCommand command) {
        validateBankCard(command);

        var existing = profileBankCardRepository.findByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return toBankCardSaveResult(command.requestId(), command.cardNumber());
        }

        String normalizedCardNumber = CardNumberSupport.normalize(command.cardNumber());
        String cardNoHash = CardNumberSupport.sha256Hex(normalizedCardNumber);
        var boundElsewhere = profileBankCardRepository.findByCardNoHash(cardNoHash);
        if (boundElsewhere.isPresent() && boundElsewhere.get().profileId() != profileId) {
            throw new ApiException(ApiCode.BANK_CARD_ALREADY_BOUND);
        }

        profileSyncOrchestrator.syncNow(new ProfileSyncJob(
                profileId,
                partnerUserId,
                command.requestId(),
                ProfileSyncModule.BANK_CARD,
                command.device(),
                new ProfileSyncPayload.BankCardProfilePayload(command.bankCode().trim(), normalizedCardNumber)
        ));

        persistLatestDevice(profileId, command.requestId(), command.device());

        EncryptedField encryptedCardNumber = sensitiveFieldEncryptor.encrypt(normalizedCardNumber);
        profileBankCardRepository.upsert(new ProfileBankCardData(
                profileId,
                command.bankCode().trim(),
                encryptedCardNumber,
                cardNoHash,
                CardNumberSupport.VERIFY_PASSED,
                null,
                MODULE_COMPLETED,
                command.requestId()
        ));

        return toBankCardSaveResult(command.requestId(), normalizedCardNumber);
    }

    private void validateBankCard(BankCardSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.bankCode() == null || command.bankCode().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.cardNumber() == null || command.cardNumber().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (CardNumberSupport.normalize(command.cardNumber()).length() > 32) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!bankReferenceFacade.isValidBankCode(command.bankCode())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
    }

    private static BankCardSaveResult toBankCardSaveResult(String requestId, String cardNumber) {
        return new BankCardSaveResult(
                requestId,
                CardNumberSupport.VERIFY_PASSED,
                CardNumberSupport.mask(cardNumber)
        );
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

    private void validateWork(WorkSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        profileEnumValidator.validateIndustry(command.industry());
        if (command.companyName() == null || command.companyName().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.companyName().trim().length() > 128) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        areaHierarchyValidator.validateResidentialHierarchy(
                command.workProvinceCode(),
                command.workCityCode(),
                command.workDistrictCode()
        );
        if (command.workAddress() == null || command.workAddress().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.workAddress().trim().length() > 512) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        IncomeValidator.validate(command.income());
        PaydayValidator.validate(command.payday());
        profileEnumValidator.validateProfessionDegree(command.professionDegree());
    }

    private static String normalizeEmail(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return null;
        }
        return userEmail.trim();
    }

    private void persistLatestDevice(long profileId, String requestId, LenderDeviceContext device) {
        profileDeviceRepository.upsert(new ProfileDeviceData(
                profileId,
                device.deviceNo().trim(),
                device.systemPlatform().trim().toLowerCase(),
                device.resolvedClientAppName(),
                device.appVersion().trim(),
                device.packageName().trim(),
                normalizeOptional(device.adId()),
                device.deviceOtherInfo(),
                requestId
        ));
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record PersonalSaveCommand(
            String requestId,
            String provinceCode,
            String cityCode,
            String districtCode,
            String address,
            Integer educationDegree,
            String motherSurname,
            String userEmail,
            LenderDeviceContext device
    ) {
    }

    public record PersonalSaveResult(String requestId, String moduleStatus) {
    }

    public record WorkSaveCommand(
            String requestId,
            Integer industry,
            String companyName,
            String workProvinceCode,
            String workCityCode,
            String workDistrictCode,
            String workAddress,
            String income,
            Integer payday,
            Integer professionDegree,
            LenderDeviceContext device
    ) {
    }

    public record WorkSaveResult(String requestId, String moduleStatus) {
    }

    public record ContactItemCommand(
            Integer relationship,
            String contactName,
            String contactMobile
    ) {
    }

    public record ContactsSaveCommand(
            String requestId,
            List<ContactItemCommand> contacts,
            LenderDeviceContext device
    ) {
    }

    public record ContactsSaveResult(String requestId, String moduleStatus) {
    }

    public record BankCardSaveCommand(
            String requestId,
            String bankCode,
            String cardNumber,
            LenderDeviceContext device
    ) {
    }

    public record BankCardSaveResult(String requestId, String verifyStatus, String cardNoMasked) {
    }
}
