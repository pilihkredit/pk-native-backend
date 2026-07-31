package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ocr.OcrCallContext;
import com.pk.core.profile.ocr.OcrCallContextHolder;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.ocr.TrustDecisionSessionState;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.core.profile.port.TrustDecisionSessionStore;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.ocr.OcrImageSupport;
import com.pk.infra.ocr.TrustDecisionLenderRawOcrDetailBuilder;
import com.pk.infra.ocr.TrustDecisionProperties;
import java.time.Instant;
import java.util.function.Supplier;

public class TrustDecisionIdentityFacade {
    private static final String CHANNEL = "tongdun";
    private static final String MODULE_COMPLETED = "COMPLETED";

    private final TrustDecisionKycPort trustDecisionKycPort;
    private final TrustDecisionSessionStore sessionStore;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final BiometricImageStore biometricImageStore;
    private final IdentityVerificationCompletionService completionService;
    private final Supplier<TrustDecisionProperties> propertiesSupplier;
    private final ObjectMapper objectMapper;

    public TrustDecisionIdentityFacade(
            TrustDecisionKycPort trustDecisionKycPort,
            TrustDecisionSessionStore sessionStore,
            ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore,
            IdentityVerificationCompletionService completionService,
            TrustDecisionProperties properties
    ) {
        this(
                trustDecisionKycPort,
                sessionStore,
                profileIdentityRepository,
                sensitiveFieldEncryptor,
                biometricImageStore,
                completionService,
                properties,
                new ObjectMapper()
        );
    }

    public TrustDecisionIdentityFacade(
            TrustDecisionKycPort trustDecisionKycPort,
            TrustDecisionSessionStore sessionStore,
            ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore,
            IdentityVerificationCompletionService completionService,
            TrustDecisionProperties properties,
            ObjectMapper objectMapper
    ) {
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.sessionStore = sessionStore;
        this.profileIdentityRepository = profileIdentityRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.biometricImageStore = biometricImageStore;
        this.completionService = completionService;
        this.propertiesSupplier = () -> properties;
        this.objectMapper = objectMapper;
    }

    public TrustDecisionIdentityFacade(
            TrustDecisionKycPort trustDecisionKycPort,
            TrustDecisionSessionStore sessionStore,
            ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore,
            IdentityVerificationCompletionService completionService,
            com.pk.infra.ocr.OcrProviderConfigLoader configLoader,
            ObjectMapper objectMapper
    ) {
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.sessionStore = sessionStore;
        this.profileIdentityRepository = profileIdentityRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.biometricImageStore = biometricImageStore;
        this.completionService = completionService;
        this.propertiesSupplier = configLoader::loadTrustDecision;
        this.objectMapper = objectMapper;
    }

    public OcrCheckResult ocrCheck(
            long userId,
            String partnerUserId,
            String mobileNo,
            String imageBase64,
            String clientRequestId,
            String traceId
    ) {
        byte[] image = OcrImageSupport.decodeBase64Image(
                imageBase64, propertiesSupplier.get().maxImageBytes());
        TrustDecisionKycPort.OcrResult result;
        Long auditId;
        setContext(userId, partnerUserId, mobileNo, clientRequestId, traceId);
        try {
            result = trustDecisionKycPort.checkIdentityCard(image);
            auditId = OcrCallContextHolder.lastVendorCallLogId();
        } finally {
            OcrCallContextHolder.clear();
        }
        if ("fail".equalsIgnoreCase(result.result())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        OcrSessionState.OcrParsedFields parsed = result.parsed();
        if (parsed == null || isBlank(parsed.ocrName()) || isBlank(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        if (!EktpValidator.isValid(parsed.ocrIdNo())) {
            throw new ApiException(ApiCode.INVALID_EKTP_FORMAT);
        }
        String imageRef = biometricImageStore.store(
                requireMobile(mobileNo), com.pk.core.profile.BiometricImageKind.ID_CARD, image);
        sessionStore.save(userId, new TrustDecisionSessionState(
                true,
                false,
                null,
                null,
                null,
                result.rawJson(),
                parsed,
                imageRef,
                auditId,
                Instant.now()
        ));
        return new OcrCheckResult(result.result(), result.sequenceId(), parsed);
    }

    public LivenessLicenseResult obtainLivenessLicense(
            long userId,
            String partnerUserId,
            String mobileNo,
            int sessionDurationSeconds,
            String clientRequestId,
            String traceId
    ) {
        if (sessionDurationSeconds <= 0 || sessionDurationSeconds > 86_400) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "sessionDurationSeconds is invalid");
        }
        setContext(userId, partnerUserId, mobileNo, clientRequestId, traceId);
        try {
            TrustDecisionKycPort.LivenessLicense result =
                    trustDecisionKycPort.obtainLivenessLicense(sessionDurationSeconds);
            return new LivenessLicenseResult(
                    result.license(), result.expiryTimestamp(), result.sequenceId());
        } finally {
            OcrCallContextHolder.clear();
        }
    }

    public FaceRecognitionResult completeInteractiveLiveness(
            long userId,
            String partnerUserId,
            String mobileNo,
            String requestId,
            String livenessId,
            LenderDeviceContext device,
            String traceId
    ) {
        if (isBlank(requestId) || isBlank(livenessId)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        ProfileSyncPayloadLoader.validateDevice(device);
        ProfileIdentityData identity = profileIdentityRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS, "identity basic info required"));
        if (requestId.equals(identity.lastRequestId()) && MODULE_COMPLETED.equals(identity.moduleStatus())) {
            return new FaceRecognitionResult(requestId, null, 0D, null, MODULE_COMPLETED, null);
        }
        TrustDecisionSessionState session = requireOcrSession(userId);
        String normalizedMobileNo = requireMobile(mobileNo);
        TrustDecisionKycPort.SdkLivenessResult livenessResult;
        setContext(userId, partnerUserId, normalizedMobileNo, requestId, traceId);
        try {
            livenessResult = trustDecisionKycPort.retrieveLivenessResult(livenessId.trim());
        } finally {
            OcrCallContextHolder.clear();
        }
        byte[] idCardImage = biometricImageStore.load(session.idCardImageEncryptedRef());
        if (idCardImage == null || idCardImage.length == 0) {
            throw new ApiException(ApiCode.OCR_SESSION_INVALID);
        }
        String plainIdNo = sensitiveFieldEncryptor.decrypt(identity.idNo()).trim();
        var completion = completionService.complete(new IdentityVerificationCompletionCommand(
                userId,
                partnerUserId,
                normalizedMobileNo,
                requestId.trim(),
                identity.fullName().trim(),
                plainIdNo,
                identity.idNo(),
                identity.idNoHash(),
                CHANNEL,
                session.ocrVendorCallLogId(),
                session.parsed(),
                TrustDecisionLenderRawOcrDetailBuilder.build(objectMapper, session.ocrRawJson()),
                idCardImage,
                livenessResult.faceImage(),
                livenessId.trim(),
                device
        ));
        return new FaceRecognitionResult(
                requestId,
                livenessResult.result(),
                0D,
                livenessResult.sequenceId(),
                completion.moduleStatus(),
                completion.lenderResponse()
        );
    }

    private TrustDecisionSessionState requireOcrSession(long userId) {
        TrustDecisionSessionState state = sessionStore.find(userId)
                .orElseThrow(() -> new ApiException(ApiCode.OCR_SESSION_INVALID));
        if (!state.ocrCompleted() || state.parsed() == null) {
            throw new ApiException(ApiCode.OCR_SESSION_INVALID);
        }
        return state;
    }

    private static void setContext(
            long userId,
            String partnerUserId,
            String mobileNo,
            String clientRequestId,
            String traceId
    ) {
        OcrCallContextHolder.set(OcrCallContext.of(
                userId, partnerUserId, requireMobile(mobileNo), clientRequestId, traceId));
    }

    private static String requireMobile(String mobileNo) {
        if (isBlank(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "mobileNo is required");
        }
        return mobileNo.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record OcrCheckResult(
            String result,
            String sequenceId,
            OcrSessionState.OcrParsedFields parsed
    ) {
    }

    public record LivenessLicenseResult(String license, long expiryTimestamp, String sequenceId) {
    }

    public record FaceRecognitionResult(
            String requestId,
            String result,
            double similarity,
            String sequenceId,
            String moduleStatus,
            JsonNode lenderResponse
    ) {
    }
}
