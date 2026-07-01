package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.ocr.OcrFieldParser;
import com.pk.infra.ocr.OcrImageSupport;
import com.pk.infra.ocr.OcrProperties;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class IdentityOcrFacade {
    public static final String MODULE_COMPLETED = "COMPLETED";
    public static final String OCR_CHANNEL = "advanceAi";

    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final OcrSessionStore ocrSessionStore;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final UserDeviceWriter userDeviceWriter;
    private final ProfileVersionRepository profileVersionRepository;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final JdbcTemplate jdbcTemplate;
    private final OcrProperties ocrProperties;
    private final ObjectMapper objectMapper;

    public IdentityOcrFacade(
            AdvanceAiOcrPort advanceAiOcrPort,
            OcrSessionStore ocrSessionStore,
            ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            UserDeviceWriter userDeviceWriter,
            ProfileVersionRepository profileVersionRepository,
            UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            JdbcTemplate jdbcTemplate,
            OcrProperties ocrProperties,
            ObjectMapper objectMapper
    ) {
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.ocrSessionStore = ocrSessionStore;
        this.profileIdentityRepository = profileIdentityRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.userDeviceWriter = userDeviceWriter;
        this.profileVersionRepository = profileVersionRepository;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.jdbcTemplate = jdbcTemplate;
        this.ocrProperties = ocrProperties;
        this.objectMapper = objectMapper;
    }

    public LicenseTokenResult getLicenseToken(long profileId, Long licenseEffectiveSeconds) {
        AdvanceAiOcrPort.LicenseTokenResult result = advanceAiOcrPort.getLicenseToken(licenseEffectiveSeconds);
        OcrSessionState current = ocrSessionStore.find(profileId).orElse(emptySession());
        ocrSessionStore.save(profileId, new OcrSessionState(
                true,
                current.ocrCheckCompleted(),
                current.livenessPassed(),
                current.livenessScore(),
                current.ocrRawJson(),
                current.parsed(),
                current.idCardImageBase64(),
                Instant.now()
        ));
        return new LicenseTokenResult(result.licenseToken(), result.effectiveSeconds());
    }

    public OcrCheckResult ocrCheck(long profileId, String imageBase64) {
        byte[] imageBytes = OcrImageSupport.decodeBase64Image(imageBase64, ocrProperties.maxImageBytes());
        String ocrDataJson = advanceAiOcrPort.ocrCheckIdCard(imageBytes);
        JsonNode ocrData;
        try {
            ocrData = objectMapper.readTree(ocrDataJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
        OcrSessionState.OcrParsedFields parsed = OcrFieldParser.parse(ocrData);
        if (parsed == null || isBlank(parsed.ocrName()) || isBlank(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        String rawJson = ocrDataJson;
        OcrSessionState current = ocrSessionStore.find(profileId).orElse(emptySession());
        ocrSessionStore.save(profileId, new OcrSessionState(
                current.licenseObtained(),
                true,
                current.livenessPassed(),
                current.livenessScore(),
                rawJson,
                parsed,
                OcrImageSupport.stripDataUriPrefix(imageBase64),
                Instant.now()
        ));
        return toOcrCheckResult(parsed);
    }

    public LivenessCheckResult livenessCheck(long profileId, String livenessId) {
        if (isBlank(livenessId)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "livenessId is required");
        }
        AdvanceAiOcrPort.LivenessResult result = advanceAiOcrPort.livenessCheck(livenessId.trim());
        boolean passed = result.livenessScore() >= ocrProperties.livenessThreshold();
        if (!passed) {
            throw new ApiException(ApiCode.OCR_LIVENESS_FAILED);
        }
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
                current.idCardImageBase64(),
                Instant.now()
        ));
        return new LivenessCheckResult(result.livenessScore(), true, ocrProperties.livenessThreshold());
    }

    public FaceRecognitionResult faceRecognition(
            long profileId,
            String partnerUserId,
            FaceRecognitionCommand command
    ) {
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
        String idCardBase64 = command.idCardImageBase64() == null || command.idCardImageBase64().isBlank()
                ? session.idCardImageBase64()
                : OcrImageSupport.stripDataUriPrefix(command.idCardImageBase64());
        if (isBlank(idCardBase64)) {
            throw new ApiException(ApiCode.OCR_SESSION_INVALID);
        }
        byte[] idCardImage = OcrImageSupport.decodeBase64Image(idCardBase64, ocrProperties.maxImageBytes());

        AdvanceAiOcrPort.FaceCompareResult compareResult = advanceAiOcrPort.compareFaces(idCardImage, faceImage);
        boolean passed = compareResult.similarity() >= ocrProperties.faceThreshold();
        if (!passed) {
            throw new ApiException(ApiCode.OCR_FACE_RECOGNITION_FAILED);
        }

        String faceBase64 = OcrImageSupport.stripDataUriPrefix(command.faceImageBase64());
        ProfileSyncPayload.IdentityProfilePayload payload = buildIdentityPayload(parsed, session, faceBase64, idCardBase64);

        EncryptedField encryptedIdNo = sensitiveFieldEncryptor.encrypt(parsed.ocrIdNo().trim());
        profileIdentityRepository.upsert(new ProfileIdentityData(
                profileId,
                parsed.ocrName().trim(),
                encryptedIdNo,
                EktpValidator.hash(parsed.ocrIdNo()),
                MODULE_COMPLETED,
                command.requestId().trim(),
                null,
                null
        ));
        persistIdentityAsset(profileId, parsed, session, encryptedIdNo);
        userDeviceWriter.upsertFromRequest(profileId, partnerUserId, command.requestId(), command.device());

        var syncResult = profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                profileId,
                partnerUserId,
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
     * Local/dev only: push identity to lender without Advance.ai OCR flow.
     */
    public DevLenderSyncResult devSyncIdentityToLender(
            long profileId,
            String partnerUserId,
            DevLenderSyncCommand command
    ) {
        if (!ocrProperties.devLenderSyncEnabled()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "dev lender sync is disabled");
        }
        if (isBlank(command.requestId())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "requestId is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
        ProfileSyncPayload.IdentityProfilePayload payload = buildDevIdentityPayload(command);
        var syncResult = profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                profileId,
                partnerUserId,
                command.requestId().trim(),
                ProfileSyncModule.IDENTITY,
                command.device(),
                payload
        ));
        return new DevLenderSyncResult(
                command.requestId().trim(),
                parseLenderResponse(syncResult.responseDataJson())
        );
    }

    private ProfileSyncPayload.IdentityProfilePayload buildDevIdentityPayload(DevLenderSyncCommand command) {
        String ocrName = requireText(command.ocrName(), "ocrName");
        String ocrIdNo = requireText(command.ocrIdNo(), "ocrIdNo");
        String faceBase64 = command.faceBase64() == null ? "" : command.faceBase64().trim();
        String idCardBase64 = command.idCardBase64() == null ? "" : command.idCardBase64().trim();
        String rawOcrDetail = buildDevRawOcrDetail(command);
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
        try {
            ObjectNode node = objectMapper.createObjectNode();
            putIfPresent(node, "ocrName", command.ocrName());
            putIfPresent(node, "ocrIdNo", command.ocrIdNo());
            putIfPresent(node, "gender", command.gender());
            putIfPresent(node, "religion", command.religion());
            putIfPresent(node, "maritalStatus", command.maritalStatus());
            putIfPresent(node, "birthday", command.birthday());
            putIfPresent(node, "birthPlace", command.birthPlace());
            putIfPresent(node, "address", command.address());
            putIfPresent(node, "occupation", command.occupation());
            putIfPresent(node, "nationality", command.nationality());
            putIfPresent(node, "bloodType", command.bloodType());
            putIfPresent(node, "expiryDate", command.expiryDate());
            putIfPresent(node, "province", command.province());
            putIfPresent(node, "city", command.city());
            putIfPresent(node, "district", command.district());
            putIfPresent(node, "ocrChannel", OCR_CHANNEL);
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
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

    private void persistIdentityAsset(
            long profileId,
            OcrSessionState.OcrParsedFields parsed,
            OcrSessionState session,
            EncryptedField encryptedIdNo
    ) {
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                List.of("identity"),
                "IDENTITY_OCR"
        );
        String ocrResultJson = buildOcrResultJson(parsed, session.ocrRawJson());
        jdbcTemplate.update(
                """
                INSERT INTO user_identity_asset (
                    profile_id,
                    profile_version_id,
                    id_card_hash,
                    id_card_ciphertext,
                    id_card_nonce,
                    id_card_tag,
                    full_name,
                    id_card_image_encrypted_ref,
                    face_photo_image_encrypted_ref,
                    encryption_key_ref,
                    ocr_channel,
                    ocr_result_json
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                profileId,
                profileVersionId,
                EktpValidator.hash(parsed.ocrIdNo()),
                encryptedIdNo.ciphertextBase64(),
                encryptedIdNo.nonce(),
                encryptedIdNo.tag(),
                parsed.ocrName().trim(),
                "session://id-card/" + profileId,
                "session://face/" + profileId,
                "pk-field-encryption-key",
                OCR_CHANNEL,
                ocrResultJson
        );
    }

    private String buildOcrResultJson(OcrSessionState.OcrParsedFields parsed, String rawOcrDetail) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            putIfPresent(node, "ocrName", parsed.ocrName());
            putIfPresent(node, "ocrIdNo", parsed.ocrIdNo());
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
        return new ProfileSyncPayload.IdentityProfilePayload(
                parsed.ocrName().trim(),
                parsed.ocrIdNo().trim(),
                faceBase64,
                idCardBase64,
                session.ocrRawJson(),
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
