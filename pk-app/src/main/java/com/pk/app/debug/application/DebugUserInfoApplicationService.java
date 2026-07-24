package com.pk.app.debug.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugUserInfoResponse;
import com.pk.app.debug.support.DebugImageDataUrlSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.infra.debug.mapper.DebugUserInfoReadMapper;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DebugUserInfoApplicationService {
    private final UserAuthRepository userAuthRepository;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final BiometricImageStore biometricImageStore;
    private final OcrSessionStore ocrSessionStore;
    private final ObjectMapper objectMapper;
    private final DebugUserInfoReadMapper readMapper;
    private final DebugUserProgressProperties properties;

    public DebugUserInfoApplicationService(
            UserAuthRepository userAuthRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            ProfileIdentityRepository profileIdentityRepository,
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore,
            OcrSessionStore ocrSessionStore,
            ObjectMapper objectMapper,
            DebugUserInfoReadMapper readMapper,
            DebugUserProgressProperties properties
    ) {
        this.userAuthRepository = userAuthRepository;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.profileIdentityRepository = profileIdentityRepository;
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.biometricImageStore = biometricImageStore;
        this.ocrSessionStore = ocrSessionStore;
        this.objectMapper = objectMapper;
        this.readMapper = readMapper;
        this.properties = properties;
    }

    public DebugUserInfoResponse query(String debugToken, String mobileNo) {
        validateToken(debugToken);
        if (mobileNo == null || mobileNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return userAuthRepository.findByMobileNo(mobileNo.trim())
                .map(this::buildResponse)
                .orElseGet(DebugUserInfoResponse::notFound);
    }

    private void validateToken(String debugToken) {
        if (!properties.enabled() || !properties.tokenConfigured()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if (debugToken == null || !properties.token().equals(debugToken.trim())) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private DebugUserInfoResponse buildResponse(UserProfileSummary user) {
        long profileId = user.profileId();
        DebugUserInfoReadMapper.UserAccountRecord account = readMapper.findAccountByProfileId(profileId);
        OnboardingProgressFacade.OnboardingProgressResult progress =
                onboardingProgressFacade.getProgress(profileId, user.partnerUserId());
        Optional<OcrSessionState> ocrSession = ocrSessionStore.find(profileId);
        DebugUserInfoReadMapper.IdentityAssetRecord identityAsset =
                readMapper.findLatestIdentityAssetByProfileId(profileId);

        return new DebugUserInfoResponse(
                true,
                toAccountInfo(account),
                new DebugUserInfoResponse.ProgressInfo(
                        progress.kycStatus(),
                        progress.completedModules(),
                        progress.missingModules()
                ),
                buildIdentityInfo(
                        profileIdentityRepository.findByProfileId(profileId).orElse(null),
                        identityAsset,
                        ocrSession.orElse(null)
                ),
                profilePersonalRepository.findByProfileId(profileId).map(this::toPersonalInfo).orElse(null),
                toContactsInfo(profileId),
                profileBankCardRepository.findDefaultByProfileId(profileId).map(this::toBankCardInfo).orElse(null),
                readMapper.findDevicesByProfileId(profileId).stream().map(this::toDeviceInfo).toList(),
                ocrSession.map(this::toOcrSessionInfo).orElse(null)
        );
    }

    private static DebugUserInfoResponse.AccountInfo toAccountInfo(DebugUserInfoReadMapper.UserAccountRecord account) {
        if (account == null) {
            return null;
        }
        return new DebugUserInfoResponse.AccountInfo(
                account.profileId(),
                account.partnerUserId(),
                account.externalUserId(),
                account.mobileNo(),
                account.email(),
                account.whatsApp(),
                account.kycStatus(),
                account.lastSyncedAt(),
                account.createdAt(),
                account.updatedAt()
        );
    }

    private DebugUserInfoResponse.IdentityInfo buildIdentityInfo(
            ProfileIdentityData profileIdentity,
            DebugUserInfoReadMapper.IdentityAssetRecord identityAsset,
            OcrSessionState ocrSession
    ) {
        if (profileIdentity == null && identityAsset == null && ocrSession == null) {
            return null;
        }

        OcrSessionState.OcrParsedFields parsed = ocrSession == null ? null : ocrSession.parsed();
        JsonNode ocrJson = null;

        String idCardRef = firstNonBlank(
                identityAsset == null ? null : identityAsset.idCardImageEncryptedRef(),
                ocrSession == null ? null : ocrSession.idCardImageEncryptedRef()
        );
        String faceRef = identityAsset == null ? null : identityAsset.facePhotoImageEncryptedRef();

        String fullName = firstNonBlank(
                profileIdentity == null ? null : profileIdentity.fullName(),
                identityAsset == null ? null : identityAsset.fullName(),
                text(parsed, "ocrName"),
                jsonText(ocrJson, "ocrName")
        );

        return new DebugUserInfoResponse.IdentityInfo(
                profileIdentity == null ? null : profileIdentity.moduleStatus(),
                profileIdentity == null ? null : profileIdentity.lastRequestId(),
                fullName,
                profileIdentity == null ? null : decryptField(profileIdentity.idNo()),
                profileIdentity == null ? null : profileIdentity.idNoHash(),
                firstNonBlank(text(parsed, "gender"), jsonText(ocrJson, "gender")),
                firstNonBlank(text(parsed, "religion"), jsonText(ocrJson, "religion")),
                firstNonBlank(text(parsed, "maritalStatus"), jsonText(ocrJson, "maritalStatus")),
                firstNonBlank(text(parsed, "birthday"), jsonText(ocrJson, "birthday")),
                firstNonBlank(text(parsed, "birthPlace"), jsonText(ocrJson, "birthPlace")),
                firstNonBlank(text(parsed, "address"), jsonText(ocrJson, "address")),
                firstNonBlank(text(parsed, "occupation"), jsonText(ocrJson, "occupation")),
                firstNonBlank(text(parsed, "nationality"), jsonText(ocrJson, "nationality")),
                firstNonBlank(text(parsed, "bloodType"), jsonText(ocrJson, "bloodType")),
                firstNonBlank(text(parsed, "expiryDate"), jsonText(ocrJson, "expiryDate")),
                text(parsed, "province"),
                text(parsed, "city"),
                text(parsed, "district"),
                identityAsset == null ? null : identityAsset.ocrChannel(),
                identityAsset == null ? null : identityAsset.ocrVendorCallLogId(),
                identityAsset == null ? null : identityAsset.externalInteractionId(),
                loadImageDataUrl(idCardRef),
                loadImageDataUrl(faceRef),
                identityAsset == null ? null : identityAsset.createdAt()
        );
    }

    private DebugUserInfoResponse.PersonalInfo toPersonalInfo(ProfilePersonalData data) {
        return new DebugUserInfoResponse.PersonalInfo(
                data.moduleStatus(),
                data.lastRequestId(),
                data.educationDegree(),
                data.industry(),
                data.income(),
                decryptField(data.motherSurname()),
                data.userEmail()
        );
    }

    private DebugUserInfoResponse.ContactsInfo toContactsInfo(long profileId) {
        return profileContactRepository.findModuleByProfileId(profileId)
                .map(module -> new DebugUserInfoResponse.ContactsInfo(
                        module.moduleStatus(),
                        module.lastRequestId(),
                        profileContactRepository.findContactsByProfileId(profileId).stream()
                                .map(this::toContactItem)
                                .toList()
                ))
                .orElse(null);
    }

    private DebugUserInfoResponse.ContactItem toContactItem(ProfileContactData contact) {
        return new DebugUserInfoResponse.ContactItem(
                contact.sortNo(),
                contact.relationship(),
                contact.contactName(),
                contact.contactMobile()
        );
    }

    private DebugUserInfoResponse.BankCardInfo toBankCardInfo(ProfileBankCardData data) {
        return new DebugUserInfoResponse.BankCardInfo(
                data.moduleStatus(),
                data.lastRequestId(),
                data.bankCode(),
                decryptField(data.cardNumber()),
                data.cardNoHash(),
                data.verifyStatus(),
                data.verifyErrorCode()
        );
    }

    private DebugUserInfoResponse.DeviceInfo toDeviceInfo(DebugUserInfoReadMapper.DeviceRecord device) {
        return new DebugUserInfoResponse.DeviceInfo(
                device.deviceNo(),
                device.systemPlatform(),
                device.appName(),
                device.appVersion(),
                device.packageName(),
                device.phoneBrand(),
                device.phoneBrandModel(),
                device.mac(),
                device.systemVersion(),
                device.deliveryPlatform(),
                device.cpuCores(),
                device.memoryTotal(),
                device.sdCardTotal(),
                device.adId(),
                device.idfv(),
                device.idfa(),
                device.extParam(),
                device.ip(),
                device.deviceJson(),
                device.lastRequestId(),
                device.updatedAt()
        );
    }

    private DebugUserInfoResponse.OcrSessionInfo toOcrSessionInfo(OcrSessionState session) {
        return new DebugUserInfoResponse.OcrSessionInfo(
                session.licenseObtained(),
                session.ocrCheckCompleted(),
                session.livenessPassed(),
                session.livenessScore(),
                session.ocrRawJson(),
                session.updatedAt()
        );
    }

    private String loadImageDataUrl(String encryptedRef) {
        if (encryptedRef == null || encryptedRef.isBlank()) {
            return null;
        }
        try {
            return DebugImageDataUrlSupport.toDataUrl(biometricImageStore.load(encryptedRef.trim()));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private JsonNode parseOcrResultJson(String ocrResultJson) {
        if (ocrResultJson == null || ocrResultJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(ocrResultJson);
        } catch (Exception exception) {
            return null;
        }
    }

    private static String text(OcrSessionState.OcrParsedFields parsed, String field) {
        if (parsed == null) {
            return null;
        }
        return switch (field) {
            case "ocrName" -> parsed.ocrName();
            case "gender" -> parsed.gender();
            case "religion" -> parsed.religion();
            case "maritalStatus" -> parsed.maritalStatus();
            case "birthday" -> parsed.birthday();
            case "birthPlace" -> parsed.birthPlace();
            case "address" -> parsed.address();
            case "occupation" -> parsed.occupation();
            case "nationality" -> parsed.nationality();
            case "bloodType" -> parsed.bloodType();
            case "expiryDate" -> parsed.expiryDate();
            case "province" -> parsed.province();
            case "city" -> parsed.city();
            case "district" -> parsed.district();
            default -> null;
        };
    }

    private static String jsonText(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String decryptField(EncryptedField field) {
        if (field == null) {
            return null;
        }
        try {
            return sensitiveFieldEncryptor.decrypt(field);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
