package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileLoginLogData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.reference.BankReferenceFacade;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileServiceFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    private static final int MIN_CONTACT_COUNT = 2;

    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileLoginLogRepository profileLoginLogRepository;
    private final UserDeviceWriter userDeviceWriter;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileEnumValidator profileEnumValidator;
    private final BankReferenceFacade bankReferenceFacade;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final UserProfileBindingRepository userProfileBindingRepository;

    public ProfileServiceFacade(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileLoginLogRepository profileLoginLogRepository,
            UserDeviceWriter userDeviceWriter,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator,
            BankReferenceFacade bankReferenceFacade,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            OnboardingProgressFacade onboardingProgressFacade,
            UserProfileBindingRepository userProfileBindingRepository
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileLoginLogRepository = profileLoginLogRepository;
        this.userDeviceWriter = userDeviceWriter;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.profileEnumValidator = profileEnumValidator;
        this.bankReferenceFacade = bankReferenceFacade;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.userProfileBindingRepository = userProfileBindingRepository;
    }

    public PersonalSaveResult savePersonal(
            long profileId,
            String partnerUserId,
            String mobileNo,
            PersonalSaveCommand command
    ) {
        validate(command);
        String normalizedMobileNo = normalizeMobile(mobileNo);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = profilePersonalRepository.findByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new PersonalSaveResult(
                    command.requestId(),
                    MODULE_COMPLETED,
                    existing.get().lastLenderResponseJson()
            );
        }

        EncryptedField motherSurname = sensitiveFieldEncryptor.encrypt(command.motherSurname().trim());
        String normalizedEmail = normalizeEmail(command.userEmail());

        profilePersonalRepository.upsert(new ProfilePersonalData(
                profileId,
                normalizedMobileNo,
                command.educationDegree(),
                command.industry(),
                command.income().trim(),
                motherSurname,
                normalizedEmail,
                MODULE_COMPLETED,
                command.requestId(),
                null,
                null
        ));

        persistDevice(profileId, partnerUserId, command.requestId(), command.device());

        if (normalizedEmail != null) {
            profilePersonalRepository.updateEmail(profileId, normalizedEmail);
        }

        com.pk.core.profile.port.LenderProfileSyncPort.LenderProfileSyncResult syncResult =
                profileSyncOrchestrator.scheduleAfterSave(ProfileSyncJob.fromStoredModule(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command.requestId(),
                ProfileSyncModule.PERSONAL,
                command.device()
        ));

        refreshUserProfileMaster(profileId, partnerUserId);

        return new PersonalSaveResult(
                command.requestId(),
                MODULE_COMPLETED,
                syncResult.responseDataJson()
        );
    }

    public ContactsSaveResult saveContacts(
            long profileId,
            String partnerUserId,
            String mobileNo,
            ContactsSaveCommand command
    ) {
        String normalizedMobileNo = normalizeMobile(mobileNo);
        validateContacts(command, normalizedMobileNo);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = profileContactRepository.findModuleByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new ContactsSaveResult(command.requestId(), MODULE_COMPLETED);
        }

        List<ProfileContactData> contacts = toContactData(normalizedMobileNo, command.contacts());
        profileContactRepository.replaceContacts(
                profileId,
                new ProfileContactsModuleData(profileId, normalizedMobileNo, MODULE_COMPLETED, command.requestId(), null, null),
                contacts
        );

        persistDevice(profileId, partnerUserId, command.requestId(), command.device());

        profileSyncOrchestrator.scheduleAfterSave(ProfileSyncJob.fromStoredModule(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command.requestId(),
                ProfileSyncModule.CONTACT,
                command.device()
        ));

        refreshUserProfileMaster(profileId, partnerUserId);

        return new ContactsSaveResult(command.requestId(), MODULE_COMPLETED);
    }

    public BankCardSaveResult saveBankCard(
            long profileId,
            String partnerUserId,
            String mobileNo,
            BankCardSaveCommand command
    ) {
        String normalizedMobileNo = normalizeMobile(mobileNo);
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

        EncryptedField encryptedCardNumber = sensitiveFieldEncryptor.encrypt(normalizedCardNumber);
        profileBankCardRepository.upsert(new ProfileBankCardData(
                profileId,
                normalizedMobileNo,
                command.bankCode().trim(),
                encryptedCardNumber,
                cardNoHash,
                CardNumberSupport.VERIFY_PASSED,
                null,
                MODULE_COMPLETED,
                command.requestId(),
                null,
                null
        ));

        persistDevice(profileId, partnerUserId, command.requestId(), command.device());

        profileSyncOrchestrator.syncNow(new ProfileSyncJob(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command.requestId(),
                ProfileSyncModule.BANK_CARD,
                command.device(),
                new ProfileSyncPayload.BankCardProfilePayload(command.bankCode().trim(), normalizedCardNumber)
        ));

        refreshUserProfileMaster(profileId, partnerUserId);

        return toBankCardSaveResult(command.requestId(), normalizedCardNumber);
    }

    public LoginLogSaveResult saveLoginLog(
            long profileId,
            String partnerUserId,
            String mobileNo,
            LoginLogSaveCommand command
    ) {
        String normalizedMobileNo = normalizeMobile(mobileNo);
        validateLoginLog(command);

        var existing = profileLoginLogRepository.findByProfileId(profileId);
        if (existing.isPresent() && command.requestId().equals(existing.get().lastRequestId())) {
            return new LoginLogSaveResult(
                    command.requestId(),
                    MODULE_COMPLETED,
                    existing.get().lastLenderResponseJson()
            );
        }

        String normalizedLoginIp = command.loginIp().trim();
        profileLoginLogRepository.upsert(new ProfileLoginLogData(
                profileId,
                normalizedMobileNo,
                command.loginType(),
                normalizedLoginIp,
                command.loginLat(),
                command.loginLng(),
                MODULE_COMPLETED,
                command.requestId(),
                null,
                null
        ));

        persistDevice(profileId, partnerUserId, command.requestId(), command.device());

        com.pk.core.profile.port.LenderProfileSyncPort.LenderProfileSyncResult syncResult =
                profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                        profileId,
                        partnerUserId,
                        normalizedMobileNo,
                        command.requestId(),
                        ProfileSyncModule.LOGIN_LOG,
                        command.device(),
                        new ProfileSyncPayload.LoginLogProfilePayload(
                                command.loginType(),
                                normalizedLoginIp,
                                command.loginLat(),
                                command.loginLng()
                        )
                ));

        return new LoginLogSaveResult(
                command.requestId(),
                MODULE_COMPLETED,
                syncResult.responseDataJson()
        );
    }

    private void validateLoginLog(LoginLogSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank() || command.requestId().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        LoginTypeValidator.validate(command.loginType());
        if (command.loginIp() == null || command.loginIp().isBlank() || command.loginIp().trim().length() > 32) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
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

    private static List<ProfileContactData> toContactData(String ownerMobileNo, List<ContactItemCommand> contacts) {
        var result = new java.util.ArrayList<ProfileContactData>(contacts.size());
        for (int index = 0; index < contacts.size(); index++) {
            ContactItemCommand contact = contacts.get(index);
            result.add(new ProfileContactData(
                    ownerMobileNo,
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
        profileEnumValidator.validateEducationDegree(command.educationDegree());
        profileEnumValidator.validateIndustry(command.industry());
        IncomeValidator.validate(command.income());
        MotherSurnameValidator.validate(command.motherSurname());
        UserEmailValidator.validateOptional(command.userEmail());
    }

    private static String normalizeEmail(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return null;
        }
        return userEmail.trim();
    }

    private void persistDevice(
            long profileId,
            String partnerUserId,
            String requestId,
            LenderDeviceContext device
    ) {
        userDeviceWriter.upsertFromRequest(profileId, partnerUserId, requestId, device);
    }

    private void refreshUserProfileMaster(long profileId, String partnerUserId) {
        OnboardingProgressFacade.OnboardingProgressResult progress = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        userProfileBindingRepository.updateKycStatus(profileId, progress.kycStatus());
    }

    public record PersonalSaveCommand(
            String requestId,
            Integer educationDegree,
            Integer industry,
            String income,
            String motherSurname,
            String userEmail,
            LenderDeviceContext device
    ) {
    }

    public record PersonalSaveResult(String requestId, String moduleStatus, String lenderResponseJson) {
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

    public record LoginLogSaveCommand(
            String requestId,
            int loginType,
            String loginIp,
            BigDecimal loginLat,
            BigDecimal loginLng,
            LenderDeviceContext device
    ) {
    }

    public record LoginLogSaveResult(String requestId, String moduleStatus, String lenderResponseJson) {
    }
}
