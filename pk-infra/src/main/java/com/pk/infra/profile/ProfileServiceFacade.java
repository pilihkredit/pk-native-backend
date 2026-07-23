package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileLoginLogData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ProfileTongdunData;
import com.pk.core.profile.port.LenderBankCardPort;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileTongdunRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.reference.BankReferenceFacade;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ProfileServiceFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    private static final int MIN_CONTACT_COUNT = 2;
    private static final Set<String> TONGDUN_SCENE_TYPES = Set.of(
            "LOGIN", "SIGNUP", "IDENTITY", "LOAN", "CREDIT"
    );

    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileLoginLogRepository profileLoginLogRepository;
    private final ProfileAfRepository profileAfRepository;
    private final ProfileTongdunRepository profileTongdunRepository;
    private final UserDeviceWriter userDeviceWriter;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileEnumValidator profileEnumValidator;
    private final BankReferenceFacade bankReferenceFacade;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final ProfileQueryFacade profileQueryFacade;
    private final LenderBankCardPort lenderBankCardPort;

    public ProfileServiceFacade(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileLoginLogRepository profileLoginLogRepository,
            ProfileAfRepository profileAfRepository,
            ProfileTongdunRepository profileTongdunRepository,
            UserDeviceWriter userDeviceWriter,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator,
            BankReferenceFacade bankReferenceFacade,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            OnboardingProgressFacade onboardingProgressFacade,
            UserProfileBindingRepository userProfileBindingRepository,
            ProfileQueryFacade profileQueryFacade,
            LenderBankCardPort lenderBankCardPort
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileLoginLogRepository = profileLoginLogRepository;
        this.profileAfRepository = profileAfRepository;
        this.profileTongdunRepository = profileTongdunRepository;
        this.userDeviceWriter = userDeviceWriter;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.profileEnumValidator = profileEnumValidator;
        this.bankReferenceFacade = bankReferenceFacade;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.profileQueryFacade = profileQueryFacade;
        this.lenderBankCardPort = lenderBankCardPort;
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

        var sameRequest = profileBankCardRepository.findByLastRequestId(command.requestId());
        if (sameRequest.isPresent()
                && sameRequest.get().profileId() == profileId
                && !sameRequest.get().deletedFlag()) {
            return toBankCardSaveResult(command.requestId(), command.cardNumber());
        }

        String normalizedCardNumber = CardNumberSupport.normalize(command.cardNumber());
        String cardNoHash = CardNumberSupport.sha256Hex(normalizedCardNumber);
        var boundByHash = profileBankCardRepository.findByCardNoHash(cardNoHash);
        if (boundByHash.isPresent() && boundByHash.get().profileId() != profileId) {
            throw new ApiException(ApiCode.BANK_CARD_ALREADY_BOUND);
        }

        EncryptedField encryptedCardNumber = sensitiveFieldEncryptor.encrypt(normalizedCardNumber);
        profileBankCardRepository.clearDefaultByProfileId(profileId);
        ProfileBankCardData cardData = new ProfileBankCardData(
                boundByHash.map(ProfileBankCardData::id).orElse(null),
                profileId,
                normalizedMobileNo,
                command.bankCode().trim(),
                encryptedCardNumber,
                cardNoHash,
                CardNumberSupport.VERIFY_PASSED,
                null,
                true,
                false,
                MODULE_COMPLETED,
                command.requestId(),
                null,
                null
        );
        if (boundByHash.isPresent() && boundByHash.get().profileId() == profileId) {
            profileBankCardRepository.updateById(cardData);
        } else {
            profileBankCardRepository.insert(cardData);
        }

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

    public BankCardDeleteResult deleteBankCard(
            long profileId,
            String partnerUserId,
            String mobileNo,
            BankCardDeleteCommand command
    ) {
        if (command == null
                || command.requestId() == null || command.requestId().isBlank()
                || command.cardNumber() == null || command.cardNumber().isBlank()
                || command.device() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        var sameRequest = profileBankCardRepository.findByLastRequestId(command.requestId().trim());
        if (sameRequest.isPresent()
                && sameRequest.get().profileId() == profileId
                && sameRequest.get().deletedFlag()) {
            return new BankCardDeleteResult(command.requestId().trim(), true);
        }

        String normalizedCardNumber = CardNumberSupport.normalize(command.cardNumber());
        String cardNoHash = CardNumberSupport.sha256Hex(normalizedCardNumber);
        ProfileBankCardData localCard = profileBankCardRepository
                .findActiveByProfileIdAndCardNoHash(profileId, cardNoHash)
                .orElseThrow(() -> new ApiException(ApiCode.BANK_CARD_NOT_FOUND));
        if (localCard.defaultFlag()) {
            throw new ApiException(ApiCode.BANK_CARD_DEFAULT_CANNOT_DELETE);
        }

        long bankCardId = resolveLenderBankCardId(partnerUserId, normalizedCardNumber);
        lenderBankCardPort.deleteBankCard(new LenderBankCardPort.DeleteBankCardCommand(partnerUserId, bankCardId));

        profileBankCardRepository.softDeleteById(localCard.id(), command.requestId().trim());
        persistDevice(profileId, partnerUserId, command.requestId().trim(), command.device());

        return new BankCardDeleteResult(command.requestId().trim(), true);
    }

    private long resolveLenderBankCardId(String partnerUserId, String normalizedCardNumber) {
        JsonNode root = profileQueryFacade.query(partnerUserId, List.of("bankCard"));
        JsonNode list = root.path("bankCardList");
        if (!list.isArray()) {
            throw new ApiException(ApiCode.BANK_CARD_NOT_FOUND);
        }
        for (Iterator<JsonNode> it = list.elements(); it.hasNext(); ) {
            JsonNode item = it.next();
            String lenderCardNumber = CardNumberSupport.normalize(item.path("cardNumber").asText(null));
            if (normalizedCardNumber.equals(lenderCardNumber) && item.hasNonNull("bankCardId")) {
                return item.get("bankCardId").asLong();
            }
        }
        throw new ApiException(ApiCode.BANK_CARD_NOT_FOUND);
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

    public AppsFlyerSaveResult saveAppsFlyerInstall(
            Long profileId,
            String partnerUserId,
            String mobileNo,
            AppsFlyerSaveCommand command
    ) {
        validateAppsFlyer(command);
        boolean loggedIn = profileId != null
                && partnerUserId != null
                && !partnerUserId.isBlank()
                && mobileNo != null
                && !mobileNo.isBlank();
        String normalizedMobileNo = loggedIn ? normalizeMobile(mobileNo) : null;

        var existing = profileAfRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return new AppsFlyerSaveResult(
                    command.requestId(),
                    MODULE_COMPLETED,
                    existing.get().lastLenderResponseJson()
            );
        }

        String deviceNo = command.device().deviceNo().trim();
        profileAfRepository.insert(new ProfileAfData(
                null,
                profileId,
                normalizedMobileNo,
                deviceNo,
                command.appsflyerId().trim(),
                trimToNull(command.advertisingId()),
                trimToNull(command.androidId()),
                trimToNull(command.attributedTouchTime()),
                trimToNull(command.gpClickTime()),
                trimToNull(command.installTime()),
                trimToNull(command.mediaSource()),
                trimToNull(command.afPrt()),
                trimToNull(command.afAdsetId()),
                trimToNull(command.afAdset()),
                trimToNull(command.afSiteid()),
                trimToNull(command.afCId()),
                trimToNull(command.campaign()),
                trimToNull(command.appVersion()),
                trimToNull(command.appId()),
                trimToNull(command.deviceType()),
                trimToNull(command.osVersion()),
                trimToNull(command.countryCode()),
                trimToNull(command.city()),
                trimToNull(command.postalCode()),
                trimToNull(command.ip()),
                trimToNull(command.operator()),
                trimToNull(command.deviceCategory()),
                trimToNull(command.platform()),
                trimToNull(command.deviceModel()),
                trimToNull(command.idfv()),
                trimToNull(command.idfa()),
                trimToNull(command.afAd()),
                trimToNull(command.afChannel()),
                trimToNull(command.attributedTouchType()),
                trimToNull(command.afAdId()),
                trimToNull(command.afAdType()),
                trimToNull(command.contributor1TouchType()),
                trimToNull(command.contributor1TouchTime()),
                trimToNull(command.contributor1AfPrt()),
                trimToNull(command.contributor1MatchType()),
                trimToNull(command.contributor1EngagementType()),
                trimToNull(command.bundleId()),
                trimToNull(command.matchType()),
                trimToNull(command.gpInstallBegin()),
                MODULE_COMPLETED,
                command.requestId().trim(),
                null,
                null
        ));

        // Store-only: lender appsFlyerInstall is attached on identity upsert.
        if (loggedIn) {
            persistDevice(profileId, partnerUserId, command.requestId(), command.device());
        }
        return new AppsFlyerSaveResult(command.requestId().trim(), MODULE_COMPLETED, null);
    }

    public TongdunSaveResult saveTongdunDevice(
            long profileId,
            String partnerUserId,
            String mobileNo,
            TongdunSaveCommand command
    ) {
        validateTongdun(command);
        String normalizedMobileNo = normalizeMobile(mobileNo);
        String sceneType = command.sceneType().trim();
        String tongdunKey = command.tongdunKey().trim();

        var existing = profileTongdunRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return new TongdunSaveResult(
                    command.requestId(),
                    MODULE_COMPLETED,
                    existing.get().lastLenderResponseJson()
            );
        }

        profileTongdunRepository.insert(new ProfileTongdunData(
                null,
                profileId,
                normalizedMobileNo,
                sceneType,
                tongdunKey,
                MODULE_COMPLETED,
                command.requestId().trim(),
                null,
                null
        ));

        persistDevice(profileId, partnerUserId, command.requestId(), command.device());

        com.pk.core.profile.port.LenderProfileSyncPort.LenderProfileSyncResult syncResult =
                profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                        profileId,
                        partnerUserId,
                        normalizedMobileNo,
                        command.requestId().trim(),
                        ProfileSyncModule.TONGDUN_DEVICE,
                        command.device(),
                        new ProfileSyncPayload.TongdunDevicePayload(sceneType, tongdunKey)
                ));

        return new TongdunSaveResult(
                command.requestId().trim(),
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

    private void validateAppsFlyer(AppsFlyerSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank() || command.requestId().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.appsflyerId() == null || command.appsflyerId().isBlank() || command.appsflyerId().trim().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "appsflyerId is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
    }

    private void validateTongdun(TongdunSaveCommand command) {
        if (command.requestId() == null || command.requestId().isBlank() || command.requestId().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.sceneType() == null || command.sceneType().isBlank()
                || !TONGDUN_SCENE_TYPES.contains(command.sceneType().trim())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "sceneType is invalid");
        }
        if (command.tongdunKey() == null || command.tongdunKey().isBlank() || command.tongdunKey().trim().length() > 256) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "tongdunKey is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    public record BankCardDeleteCommand(
            String requestId,
            String cardNumber,
            LenderDeviceContext device
    ) {
    }

    public record BankCardDeleteResult(String requestId, boolean deleted) {
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

    public record AppsFlyerSaveCommand(
            String requestId,
            String appsflyerId,
            String advertisingId,
            String androidId,
            String attributedTouchTime,
            String gpClickTime,
            String installTime,
            String mediaSource,
            String afPrt,
            String afAdsetId,
            String afAdset,
            String afSiteid,
            String afCId,
            String campaign,
            String appVersion,
            String appId,
            String deviceType,
            String osVersion,
            String countryCode,
            String city,
            String postalCode,
            String ip,
            String operator,
            String deviceCategory,
            String platform,
            String deviceModel,
            String idfv,
            String idfa,
            String afAd,
            String afChannel,
            String attributedTouchType,
            String afAdId,
            String afAdType,
            String contributor1TouchType,
            String contributor1TouchTime,
            String contributor1AfPrt,
            String contributor1MatchType,
            String contributor1EngagementType,
            String bundleId,
            String matchType,
            String gpInstallBegin,
            LenderDeviceContext device
    ) {
    }

    public record AppsFlyerSaveResult(String requestId, String moduleStatus, String lenderResponseJson) {
    }

    public record TongdunSaveCommand(
            String requestId,
            String sceneType,
            String tongdunKey,
            LenderDeviceContext device
    ) {
    }

    public record TongdunSaveResult(String requestId, String moduleStatus, String lenderResponseJson) {
    }
}
