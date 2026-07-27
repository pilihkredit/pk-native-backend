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

public class TrustDecisionIdentityFacade {
    private static final String CHANNEL = "trustDecision";
    private static final String MODULE_COMPLETED = "COMPLETED";

    private final TrustDecisionKycPort trustDecisionKycPort;
    private final TrustDecisionSessionStore sessionStore;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final BiometricImageStore biometricImageStore;
    private final IdentityVerificationCompletionService completionService;
    private final TrustDecisionProperties properties;
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
        this.properties = properties;
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
        byte[] image = OcrImageSupport.decodeBase64Image(imageBase64, properties.maxImageBytes());
        TrustDecisionKycPort.OcrResult result;
        Long auditId;
        setContext(userId, partnerUserId, mobileNo, clientRequestId, traceId);
        try {
            result = trustDecisionKycPort.checkIdentityCard(image);
            auditId = OcrCallContextHolder.lastVendorCallLogId();
        } finally {
            OcrCallContextHolder.clear();
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

    public LivenessCheckResult livenessCheck(
            long userId,
            String partnerUserId,
            String mobileNo,
            String imageBase64,
            String clientRequestId,
            String traceId
    ) {
        TrustDecisionSessionState current = requireOcrSession(userId);
        byte[] image = OcrImageSupport.decodeBase64Image(imageBase64, properties.maxImageBytes());
        TrustDecisionKycPort.LivenessResult result;
        setContext(userId, partnerUserId, mobileNo, clientRequestId, traceId);
        try {
            result = trustDecisionKycPort.checkLiveness(image);
        } finally {
            OcrCallContextHolder.clear();
        }
        sessionStore.save(userId, new TrustDecisionSessionState(
                true,
                true,
                result.result(),
                result.score(),
                result.sequenceId(),
                current.ocrRawJson(),
                current.parsed(),
                current.idCardImageEncryptedRef(),
                current.ocrVendorCallLogId(),
                Instant.now()
        ));
        return new LivenessCheckResult(result.result(), result.score(), result.sequenceId());
    }

    public FaceRecognitionResult faceRecognition(
            long userId,
            String partnerUserId,
            String mobileNo,
            FaceRecognitionCommand command,
            String traceId
    ) {
        if (isBlank(command.requestId())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "requestId is required");
        }
        ProfileSyncPayloadLoader.validateDevice(command.device());
        ProfileIdentityData identity = profileIdentityRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS, "identity basic info required"));
        if (command.requestId().equals(identity.lastRequestId())
                && MODULE_COMPLETED.equals(identity.moduleStatus())) {
            return new FaceRecognitionResult(
                    command.requestId(), null, 0D, null, MODULE_COMPLETED, null);
        }
        if (MODULE_COMPLETED.equals(identity.moduleStatus())) {
            throw new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
        }
        TrustDecisionSessionState session = requireOcrSession(userId);
        if (!session.livenessCompleted()) {
            throw new ApiException(ApiCode.OCR_SESSION_INVALID);
        }
        byte[] faceImage = OcrImageSupport.decodeBase64Image(
                command.faceImageBase64(), properties.maxImageBytes());
        byte[] idCardImage = OcrImageSupport.decodeBase64Image(
                command.idCardImageBase64(), properties.maxImageBytes());
        setContext(userId, partnerUserId, mobileNo, command.requestId(), traceId);
        TrustDecisionKycPort.FaceCompareResult compareResult;
        try {
            compareResult = trustDecisionKycPort.compareFaces(idCardImage, faceImage);
        } finally {
            OcrCallContextHolder.clear();
        }
        String plainIdNo = sensitiveFieldEncryptor.decrypt(identity.idNo()).trim();
        var completion = completionService.complete(new IdentityVerificationCompletionCommand(
                userId,
                partnerUserId,
                requireMobile(mobileNo),
                command.requestId().trim(),
                identity.fullName().trim(),
                plainIdNo,
                identity.idNo(),
                identity.idNoHash(),
                CHANNEL,
                session.ocrVendorCallLogId(),
                session.parsed(),
                TrustDecisionLenderRawOcrDetailBuilder.build(objectMapper, session.ocrRawJson()),
                idCardImage,
                faceImage,
                command.device()
        ));
        return new FaceRecognitionResult(
                command.requestId(),
                compareResult.result(),
                compareResult.similarity(),
                compareResult.sequenceId(),
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

    public record LivenessCheckResult(String result, double score, String sequenceId) {
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
            String result,
            double similarity,
            String sequenceId,
            String moduleStatus,
            JsonNode lenderResponse
    ) {
    }
}
