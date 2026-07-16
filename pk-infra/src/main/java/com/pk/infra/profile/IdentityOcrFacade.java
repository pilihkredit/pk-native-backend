package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.logging.LogContext;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ocr.OcrCallContext;
import com.pk.core.profile.ocr.OcrCallContextHolder;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.ocr.AdvanceAiLenderRawOcrDetailSupport;
import com.pk.infra.ocr.AdvanceAiRawOcrDetailBuilder;
import com.pk.infra.ocr.OcrFieldParser;
import com.pk.infra.ocr.OcrImageSupport;
import com.pk.infra.ocr.OcrProperties;
import com.pk.infra.ocr.OcrSensitiveJsonSupport;
import java.time.Instant;
import java.util.List;

public class IdentityOcrFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    public static final String OCR_CHANNEL = "advanceAi";

    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final OcrSessionStore ocrSessionStore;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final BiometricImageStore biometricImageStore;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final UserDeviceWriter userDeviceWriter;
    private final ProfileVersionRepository profileVersionRepository;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final OcrSensitiveJsonSupport sensitiveJsonSupport;
    private final OcrProperties ocrProperties;
    private final ObjectMapper objectMapper;

    public IdentityOcrFacade(
            AdvanceAiOcrPort advanceAiOcrPort,
            OcrSessionStore ocrSessionStore,
            ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            UserDeviceWriter userDeviceWriter,
            ProfileVersionRepository profileVersionRepository,
            UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            OcrSensitiveJsonSupport sensitiveJsonSupport,
            OcrProperties ocrProperties,
            ObjectMapper objectMapper
    ) {
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.ocrSessionStore = ocrSessionStore;
        this.profileIdentityRepository = profileIdentityRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.biometricImageStore = biometricImageStore;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.userDeviceWriter = userDeviceWriter;
        this.profileVersionRepository = profileVersionRepository;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.sensitiveJsonSupport = sensitiveJsonSupport;
        this.ocrProperties = ocrProperties;
        this.objectMapper = objectMapper;
    }

    public LicenseTokenResult getLicenseToken(
            long profileId,
            String partnerUserId,
            String mobileNo,
            Long licenseEffectiveSeconds,
            String clientRequestId,
            String traceId
    ) {
        String normalizedMobileNo = requireMobile(mobileNo);
        OcrCallContextHolder.set(OcrCallContext.of(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                clientRequestId,
                traceId
        ));
        try {
            AdvanceAiOcrPort.LicenseTokenResult result = advanceAiOcrPort.getLicenseToken(licenseEffectiveSeconds);
            ocrSessionStore.save(profileId, new OcrSessionState(
                    true,
                    false,
                    false,
                    null,
                    null,
                    null,
                    null,
                    Instant.now()
            ));
            return new LicenseTokenResult(result.licenseToken(), result.effectiveSeconds());
        } finally {
            OcrCallContextHolder.clear();
        }
    }

    public OcrCheckResult ocrCheck(
            long profileId,
            String partnerUserId,
            String mobileNo,
            String imageBase64,
            String clientRequestId,
            String traceId
    ) {
        String normalizedMobileNo = requireMobile(mobileNo);
        byte[] imageBytes = OcrImageSupport.decodeBase64Image(imageBase64, ocrProperties.maxImageBytes());
        OcrCallContextHolder.set(OcrCallContext.of(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                clientRequestId,
                traceId
        ));
        String ocrResponseJson;
        try {
            ocrResponseJson = advanceAiOcrPort.ocrCheckIdCard(imageBytes);
        } finally {
            OcrCallContextHolder.clear();
        }
        JsonNode ocrData = AdvanceAiLenderRawOcrDetailSupport.extractDataNode(objectMapper, ocrResponseJson);
        OcrSessionState.OcrParsedFields parsed = OcrFieldParser.parse(ocrData);
        if (parsed == null || isBlank(parsed.ocrName()) || isBlank(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        String rawJson = ocrResponseJson;
        String idCardImageEncryptedRef = biometricImageStore.store(
                normalizedMobileNo,
                BiometricImageKind.ID_CARD,
                imageBytes
        );
        OcrSessionState current = ocrSessionStore.find(profileId).orElse(emptySession());
        ocrSessionStore.save(profileId, new OcrSessionState(
                current.licenseObtained(),
                true,
                current.livenessPassed(),
                current.livenessScore(),
                rawJson,
                parsed,
                idCardImageEncryptedRef,
                Instant.now()
        ));
        return toOcrCheckResult(parsed);
    }

    public LivenessCheckResult livenessCheck(
            long profileId,
            String partnerUserId,
            String mobileNo,
            String livenessId,
            String clientRequestId,
            String traceId
    ) {
        if (isBlank(livenessId)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "livenessId is required");
        }
        String normalizedMobileNo = requireMobile(mobileNo);
        OcrCallContextHolder.set(OcrCallContext.of(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                clientRequestId,
                traceId
        ));
        try {
            AdvanceAiOcrPort.LivenessResult result = advanceAiOcrPort.livenessCheck(livenessId.trim());
            OcrSessionState current = ocrSessionStore.find(profileId)
                    .orElseThrow(() -> new ApiException(ApiCode.OCR_SESSION_INVALID));
            if (!current.ocrCheckCompleted()) {
                throw new ApiException(ApiCode.OCR_SESSION_INVALID);
            }
            ocrSessionStore.save(profileId, new OcrSessionState(
                    current.licenseObtained(),
                    current.ocrCheckCompleted(),
                    true,
                    result.livenessScore(),
                    current.ocrRawJson(),
                    current.parsed(),
                    current.idCardImageEncryptedRef(),
                    Instant.now()
            ));
            return new LivenessCheckResult(result.livenessScore(), true, ocrProperties.livenessThreshold());
        } finally {
            OcrCallContextHolder.clear();
        }
    }

    public FaceRecognitionResult faceRecognition(
            long profileId,
            String partnerUserId,
            String mobileNo,
            FaceRecognitionCommand command,
            String traceId
    ) {
        String normalizedMobileNo = normalizeMobile(mobileNo);
        if (isBlank(command.requestId())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "requestId is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = profileIdentityRepository.findByProfileId(profileId);
        if (existing.isPresent()) {
            ProfileIdentityData identity = existing.get();
            if (command.requestId().equals(identity.lastRequestId())) {
                return buildIdempotentResult(command.requestId(), identity);
            }
            if (MODULE_COMPLETED.equals(identity.moduleStatus())) {
                throw new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
            }
        }

        OcrSessionState session = ocrSessionStore.find(profileId)
                .orElseThrow(() -> new ApiException(ApiCode.OCR_SESSION_INVALID));
        if (!session.ocrCheckCompleted() || !session.livenessPassed()) {
            throw new ApiException(ApiCode.OCR_SESSION_INVALID);
        }
        OcrSessionState.OcrParsedFields parsed = session.parsed();
        if (parsed == null || isBlank(parsed.ocrName()) || isBlank(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        if (!EktpValidator.isValid(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.INVALID_EKTP_FORMAT);
        }

        byte[] faceImage = OcrImageSupport.decodeBase64Image(command.faceImageBase64(), ocrProperties.maxImageBytes());
        String idCardImageEncryptedRef;
        byte[] idCardImage;
        if (!isBlank(command.idCardImageBase64())) {
            idCardImage = OcrImageSupport.decodeBase64Image(command.idCardImageBase64(), ocrProperties.maxImageBytes());
            idCardImageEncryptedRef = biometricImageStore.store(
                    normalizedMobileNo,
                    BiometricImageKind.ID_CARD,
                    idCardImage
            );
        } else {
            idCardImageEncryptedRef = session.idCardImageEncryptedRef();
            if (isBlank(idCardImageEncryptedRef)) {
                throw new ApiException(ApiCode.OCR_SESSION_INVALID);
            }
            idCardImage = biometricImageStore.load(idCardImageEncryptedRef);
        }

        OcrCallContextHolder.set(OcrCallContext.of(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command.requestId().trim(),
                traceId
        ));
        AdvanceAiOcrPort.FaceCompareResult compareResult;
        try {
            compareResult = advanceAiOcrPort.compareFaces(idCardImage, faceImage);
        } finally {
            OcrCallContextHolder.clear();
        }

        String faceImageEncryptedRef = biometricImageStore.store(
                normalizedMobileNo,
                BiometricImageKind.FACE,
                faceImage
        );
        String faceBase64 = OcrImageSupport.encodeBase64(faceImage);
        String idCardBase64 = OcrImageSupport.encodeBase64(idCardImage);
        ProfileSyncPayload.IdentityProfilePayload payload = buildIdentityPayload(parsed, session, faceBase64, idCardBase64);

        EncryptedField encryptedIdNo = sensitiveFieldEncryptor.encrypt(parsed.ocrIdNo().trim());
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                normalizedMobileNo,
                List.of("identity"),
                "IDENTITY_OCR"
        );
        String ocrResultJson = sensitiveJsonSupport.sanitizeForStorage(
                buildOcrResultJson(parsed, lenderRawOcrDetail(session.ocrRawJson())),
                normalizedMobileNo
        );
        profileIdentityRepository.upsert(new ProfileIdentityData(
                profileId,
                normalizedMobileNo,
                parsed.ocrName().trim(),
                encryptedIdNo,
                EktpValidator.hash(parsed.ocrIdNo()),
                MODULE_COMPLETED,
                command.requestId().trim(),
                null,
                null,
                profileVersionId,
                null,
                idCardImageEncryptedRef,
                faceImageEncryptedRef,
                biometricImageStore.encryptionKeyRef(),
                OCR_CHANNEL,
                ocrResultJson
        ));
        userDeviceWriter.upsertFromRequest(profileId, partnerUserId, command.requestId(), command.device());

        var syncResult = profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command.requestId(),
                ProfileSyncModule.IDENTITY,
                command.device(),
                payload
        ));
        refreshKycStatus(profileId, partnerUserId);

        return new FaceRecognitionResult(
                command.requestId(),
                compareResult.similarity(),
                true,
                ocrProperties.faceThreshold(),
                MODULE_COMPLETED,
                parseLenderResponse(syncResult.responseDataJson())
        );
    }

    /**
     * Local/dev only: push identity to lender. When {@code idCardBase64} is provided and
     * {@code rawOcrDetail} is absent, Advance.ai OCR is invoked to capture lender-ready raw JSON.
     */
    public DevLenderSyncResult devSyncIdentityToLender(
            long profileId,
            String partnerUserId,
            String mobileNo,
            DevLenderSyncCommand command
    ) {
        String normalizedMobileNo = normalizeMobile(mobileNo);
        if (!ocrProperties.devLenderSyncEnabled()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "dev lender sync is disabled");
        }
        if (isBlank(command.requestId())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "requestId is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
        DevLenderSyncCommand resolvedCommand = resolveDevLenderSyncCommand(
                profileId,
                partnerUserId,
                normalizedMobileNo,
                command
        );
        String ocrName = requireText(resolvedCommand.ocrName(), "ocrName");
        String ocrIdNo = requireText(resolvedCommand.ocrIdNo(), "ocrIdNo");
        ProfileSyncPayload.IdentityProfilePayload payload = buildDevIdentityPayload(resolvedCommand);
        persistDevIdentityLocalState(profileId, partnerUserId, normalizedMobileNo, resolvedCommand, ocrName, ocrIdNo);

        JsonNode lenderResponse;
        try {
            var syncResult = profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                    profileId,
                    partnerUserId,
                    normalizedMobileNo,
                    resolvedCommand.requestId().trim(),
                    ProfileSyncModule.IDENTITY,
                    resolvedCommand.device(),
                    payload
            ));
            lenderResponse = parseLenderResponse(syncResult.responseDataJson());
        } catch (ApiException exception) {
            lenderResponse = buildLenderSyncFailureNode(exception);
        } catch (RuntimeException exception) {
            lenderResponse = buildLenderSyncFailureNode(
                    new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception.getMessage())
            );
        }
        return new DevLenderSyncResult(resolvedCommand.requestId().trim(), lenderResponse);
    }

    private DevLenderSyncCommand resolveDevLenderSyncCommand(
            long profileId,
            String partnerUserId,
            String mobileNo,
            DevLenderSyncCommand command
    ) {
        if (!isBlank(command.rawOcrDetail())) {
            return command;
        }
        if (isBlank(command.idCardBase64())) {
            return command;
        }
        byte[] imageBytes = OcrImageSupport.decodeBase64Image(command.idCardBase64(), ocrProperties.maxImageBytes());
        OcrCallContextHolder.set(OcrCallContext.of(
                profileId,
                partnerUserId,
                requireMobile(mobileNo),
                command.requestId(),
                LogContext.traceId()
        ));
        String ocrResponseJson;
        try {
            ocrResponseJson = advanceAiOcrPort.ocrCheckIdCard(imageBytes);
        } finally {
            OcrCallContextHolder.clear();
        }
        JsonNode ocrData = AdvanceAiLenderRawOcrDetailSupport.extractDataNode(objectMapper, ocrResponseJson);
        OcrSessionState.OcrParsedFields parsed = OcrFieldParser.parse(ocrData);
        if (parsed == null || isBlank(parsed.ocrName()) || isBlank(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        if (!EktpValidator.isValid(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.INVALID_EKTP_FORMAT);
        }
        String lenderRawOcrDetail = AdvanceAiLenderRawOcrDetailSupport.prepareForLender(objectMapper, ocrResponseJson);
        String idCardBase64 = OcrImageSupport.stripDataUriPrefix(command.idCardBase64());
        return new DevLenderSyncCommand(
                command.requestId(),
                command.faceBase64(),
                idCardBase64,
                lenderRawOcrDetail,
                firstNonBlank(command.ocrName(), parsed.ocrName()),
                firstNonBlank(command.ocrIdNo(), parsed.ocrIdNo()),
                firstNonBlank(command.gender(), parsed.gender()),
                firstNonBlank(command.religion(), parsed.religion()),
                firstNonBlank(command.maritalStatus(), parsed.maritalStatus()),
                firstNonBlank(command.birthday(), parsed.birthday()),
                firstNonBlank(command.birthPlace(), parsed.birthPlace()),
                firstNonBlank(command.address(), parsed.address()),
                firstNonBlank(command.occupation(), parsed.occupation()),
                firstNonBlank(command.nationality(), parsed.nationality()),
                firstNonBlank(command.bloodType(), parsed.bloodType()),
                firstNonBlank(command.expiryDate(), parsed.expiryDate()),
                firstNonBlank(command.province(), parsed.province()),
                firstNonBlank(command.city(), parsed.city()),
                firstNonBlank(command.district(), parsed.district()),
                command.device()
        );
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (!isBlank(preferred)) {
            return preferred.trim();
        }
        return fallback;
    }

    private void persistDevIdentityLocalState(
            long profileId,
            String partnerUserId,
            String mobileNo,
            DevLenderSyncCommand command,
            String ocrName,
            String ocrIdNo
    ) {
        EncryptedField encryptedIdNo = sensitiveFieldEncryptor.encrypt(ocrIdNo);
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                mobileNo,
                List.of("identity"),
                "IDENTITY_DEV_LENDER_SYNC"
        );
        String idCardImageEncryptedRef = storeDevImage(mobileNo, BiometricImageKind.ID_CARD, command.idCardBase64());
        String facePhotoImageEncryptedRef = storeDevImage(mobileNo, BiometricImageKind.FACE, command.faceBase64());
        String ocrResultJson = sensitiveJsonSupport.sanitizeForStorage(
                buildOcrResultJson(
                        toDevParsedFields(command),
                        lenderRawOcrDetail(buildDevRawOcrDetail(command))
                ),
                mobileNo
        );
        profileIdentityRepository.upsert(new ProfileIdentityData(
                profileId,
                mobileNo,
                ocrName,
                encryptedIdNo,
                EktpValidator.hash(ocrIdNo),
                MODULE_COMPLETED,
                command.requestId().trim(),
                null,
                null,
                profileVersionId,
                null,
                idCardImageEncryptedRef,
                facePhotoImageEncryptedRef,
                biometricImageStore.encryptionKeyRef(),
                OCR_CHANNEL,
                ocrResultJson
        ));
        userDeviceWriter.upsertFromRequest(profileId, partnerUserId, command.requestId(), command.device());
        refreshKycStatus(profileId, partnerUserId);
    }

    private JsonNode buildLenderSyncFailureNode(ApiException exception) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("syncFailed", true);
            node.put("code", exception.apiCode().code());
            node.put("msg", exception.apiCode().message());
            return node;
        } catch (Exception mappingException) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private ProfileSyncPayload.IdentityProfilePayload buildDevIdentityPayload(DevLenderSyncCommand command) {
        String ocrName = requireText(command.ocrName(), "ocrName");
        String ocrIdNo = requireText(command.ocrIdNo(), "ocrIdNo");
        String faceBase64 = command.faceBase64() == null ? "" : command.faceBase64().trim();
        String idCardBase64 = command.idCardBase64() == null ? "" : command.idCardBase64().trim();
        String rawOcrDetail = lenderRawOcrDetail(buildDevRawOcrDetail(command));
        return new ProfileSyncPayload.IdentityProfilePayload(
                ocrName,
                ocrIdNo,
                faceBase64,
                idCardBase64,
                rawOcrDetail,
                OCR_CHANNEL,
                ocrName,
                ocrIdNo,
                command.gender(),
                command.religion(),
                command.maritalStatus(),
                command.birthday(),
                command.birthPlace(),
                command.address(),
                command.occupation(),
                command.nationality(),
                command.bloodType(),
                command.expiryDate()
        );
    }

    private String buildDevRawOcrDetail(DevLenderSyncCommand command) {
        if (command.rawOcrDetail() != null && !command.rawOcrDetail().isBlank()) {
            return command.rawOcrDetail().trim();
        }
        return AdvanceAiRawOcrDetailBuilder.build(objectMapper, toDevParsedFields(command));
    }

    private String lenderRawOcrDetail(String advanceAiJson) {
        return AdvanceAiLenderRawOcrDetailSupport.prepareForLender(objectMapper, advanceAiJson);
    }

    private static OcrSessionState.OcrParsedFields toDevParsedFields(DevLenderSyncCommand command) {
        return new OcrSessionState.OcrParsedFields(
                command.ocrName(),
                command.ocrIdNo(),
                command.gender(),
                command.religion(),
                command.maritalStatus(),
                command.birthday(),
                command.birthPlace(),
                command.address(),
                command.occupation(),
                command.nationality(),
                command.bloodType(),
                command.expiryDate(),
                command.province(),
                command.city(),
                command.district()
        );
    }

    private static String requireText(String value, String field) {
        if (isBlank(value)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, field + " is required");
        }
        return value.trim();
    }

    private void refreshKycStatus(long profileId, String partnerUserId) {
        OnboardingProgressFacade.OnboardingProgressResult progress = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        userProfileBindingRepository.updateKycStatus(profileId, progress.kycStatus());
    }

    private String storeDevImage(String mobileNo, BiometricImageKind kind, String imageBase64) {
        String normalizedMobileNo = requireMobile(mobileNo);
        if (isBlank(imageBase64)) {
            return "missing://mobile/" + normalizedMobileNo + "/" + kind.objectName();
        }
        byte[] imageBytes = OcrImageSupport.decodeBase64Image(imageBase64, ocrProperties.maxImageBytes());
        return biometricImageStore.store(normalizedMobileNo, kind, imageBytes);
    }

    private String buildOcrResultJson(OcrSessionState.OcrParsedFields parsed, String rawOcrDetail) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            putIfPresent(node, "ocrName", parsed.ocrName());
            putIfPresent(node, "gender", parsed.gender());
            putIfPresent(node, "religion", parsed.religion());
            putIfPresent(node, "maritalStatus", parsed.maritalStatus());
            putIfPresent(node, "birthday", parsed.birthday());
            putIfPresent(node, "birthPlace", parsed.birthPlace());
            putIfPresent(node, "address", parsed.address());
            putIfPresent(node, "occupation", parsed.occupation());
            putIfPresent(node, "nationality", parsed.nationality());
            putIfPresent(node, "bloodType", parsed.bloodType());
            putIfPresent(node, "expiryDate", parsed.expiryDate());
            putIfPresent(node, "rawOcrDetail", rawOcrDetail);
            putIfPresent(node, "ocrChannel", OCR_CHANNEL);
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private ProfileSyncPayload.IdentityProfilePayload buildIdentityPayload(
            OcrSessionState.OcrParsedFields parsed,
            OcrSessionState session,
            String faceBase64,
            String idCardBase64
    ) {
        // §17 session stores Advance.ai full OCR response; normalize before lender upsert.
        return new ProfileSyncPayload.IdentityProfilePayload(
                parsed.ocrName().trim(),
                parsed.ocrIdNo().trim(),
                faceBase64,
                idCardBase64,
                lenderRawOcrDetail(session.ocrRawJson()),
                OCR_CHANNEL,
                parsed.ocrName(),
                parsed.ocrIdNo(),
                parsed.gender(),
                parsed.religion(),
                parsed.maritalStatus(),
                parsed.birthday(),
                parsed.birthPlace(),
                parsed.address(),
                parsed.occupation(),
                parsed.nationality(),
                parsed.bloodType(),
                parsed.expiryDate()
        );
    }

    private FaceRecognitionResult buildIdempotentResult(String requestId, ProfileIdentityData existing) {
        return new FaceRecognitionResult(
                requestId,
                0D,
                true,
                ocrProperties.faceThreshold(),
                existing.moduleStatus(),
                parseLenderResponse(existing.lastLenderResponseJson())
        );
    }

    private JsonNode parseLenderResponse(String lenderResponseJson) {
        if (lenderResponseJson == null || lenderResponseJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(lenderResponseJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private static OcrCheckResult toOcrCheckResult(OcrSessionState.OcrParsedFields parsed) {
        return new OcrCheckResult(
                parsed.ocrName(),
                parsed.ocrIdNo(),
                parsed.gender(),
                parsed.religion(),
                parsed.maritalStatus(),
                parsed.birthday(),
                parsed.birthPlace(),
                parsed.address(),
                parsed.occupation(),
                parsed.nationality(),
                parsed.bloodType(),
                parsed.expiryDate(),
                parsed.province(),
                parsed.city(),
                parsed.district()
        );
    }

    private static OcrSessionState emptySession() {
        return new OcrSessionState(false, false, false, null, null, null, null, Instant.now());
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }

    private static String normalizeMobile(String mobileNo) {
        return mobileNo == null ? "" : mobileNo.trim();
    }

    private static String requireMobile(String mobileNo) {
        String normalized = normalizeMobile(mobileNo);
        if (normalized.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "mobileNo is required");
        }
        return normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record LicenseTokenResult(String licenseToken, long effectiveSeconds) {
    }

    public record OcrCheckResult(
            String ocrName,
            String ocrIdNo,
            String gender,
            String religion,
            String maritalStatus,
            String birthday,
            String birthPlace,
            String address,
            String occupation,
            String nationality,
            String bloodType,
            String expiryDate,
            String province,
            String city,
            String district
    ) {
    }

    public record LivenessCheckResult(int livenessScore, boolean passed, int threshold) {
    }

    public record FaceRecognitionCommand(
            String requestId,
            String faceImageBase64,
            String idCardImageBase64,
            LenderDeviceContext device
    ) {
    }

    public record FaceRecognitionResult(
            String requestId,
            double similarity,
            boolean passed,
            int threshold,
            String moduleStatus,
            JsonNode lenderResponse
    ) {
    }

    public record DevLenderSyncCommand(
            String requestId,
            String faceBase64,
            String idCardBase64,
            String rawOcrDetail,
            String ocrName,
            String ocrIdNo,
            String gender,
            String religion,
            String maritalStatus,
            String birthday,
            String birthPlace,
            String address,
            String occupation,
            String nationality,
            String bloodType,
            String expiryDate,
            String province,
            String city,
            String district,
            LenderDeviceContext device
    ) {
    }

    public record DevLenderSyncResult(String requestId, JsonNode lenderResponse) {
    }
}
