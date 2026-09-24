package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.ocr.OcrCallContext;
import com.pk.core.profile.ocr.OcrCallContextHolder;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import com.pk.infra.profile.FaceComparisonBaselineResolver;
import com.pk.infra.profile.FaceVerifyTicketSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Unauthenticated face verify for device-switch login. Does not modify mobile-change face APIs.
 */
public class DeviceSwitchLoginFaceFacade {
    private static final Duration TICKET_TTL = Duration.ofMinutes(5);

    private final UserAuthRepository userAuthRepository;
    private final TrustDecisionKycPort trustDecisionKycPort;
    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final FaceComparisonBaselineResolver baselineResolver;
    private final DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository;
    private final BiometricImageStore biometricImageStore;
    private final OcrProviderConfigLoader configLoader;
    private final DeviceSwitchLoginFaceAttemptLimiter attemptLimiter;

    public DeviceSwitchLoginFaceFacade(
            UserAuthRepository userAuthRepository,
            TrustDecisionKycPort trustDecisionKycPort,
            AdvanceAiOcrPort advanceAiOcrPort,
            FaceComparisonBaselineResolver baselineResolver,
            DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository,
            BiometricImageStore biometricImageStore,
            OcrProviderConfigLoader configLoader,
            DeviceSwitchLoginFaceAttemptLimiter attemptLimiter
    ) {
        this.userAuthRepository = userAuthRepository;
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.baselineResolver = baselineResolver;
        this.deviceSwitchFaceRepository = deviceSwitchFaceRepository;
        this.biometricImageStore = biometricImageStore;
        this.configLoader = configLoader;
        this.attemptLimiter = attemptLimiter;
    }

    public FaceVerifyResult verify(String mobileNo, FaceVerifyCommand command) {
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        String requestId = requireText(command.requestId());
        String livenessId = requireText(command.livenessId());
        String deviceNo = AuthDeviceNoNormalizer.requireNonBlank(command.deviceNo());

        UserProfileSummary profile = userAuthRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));

        attemptLimiter.assertAllowed(mobileNo, deviceNo);

        Instant now = Instant.now();
        var reusable = deviceSwitchFaceRepository.findReusablePendingByUserIdAndRequestId(
                profile.userId(),
                requestId
        );
        if (reusable.isPresent()) {
            DeviceSwitchFaceVerificationRepository.FaceVerificationData ticket = reusable.get();
            return new FaceVerifyResult(
                    requestId,
                    true,
                    ticket.faceVerifyToken(),
                    FaceVerifyTicketSupport.expiresInSeconds(ticket.expiresAt(), now)
            );
        }

        OcrCallContextHolder.set(OcrCallContext.of(
                profile.userId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                requestId,
                null
        ));
        try {
            FaceComparisonBaseline baseline = baselineResolver.resolveOrThrow(profile.userId());
            byte[] baselineImage = biometricImageStore.load(baseline.faceEncryptedRef());
            TrustDecisionKycPort.SdkLivenessResult liveness =
                    trustDecisionKycPort.retrieveLivenessResult(livenessId);
            if (liveness.faceImage() == null || liveness.faceImage().length == 0
                    || "fail".equalsIgnoreCase(liveness.result())) {
                attemptLimiter.recordFailure(mobileNo, deviceNo);
                throw new ApiException(ApiCode.OCR_LIVENESS_FAILED);
            }

            String token = UUID.randomUUID().toString();
            String candidateRef = biometricImageStore.storeVersioned(
                    mobileNo,
                    BiometricImageKind.DEVICE_SWITCH_LOGIN_FACE,
                    token,
                    liveness.faceImage()
            );
            double similarity = advanceAiOcrPort.compareFaces(baselineImage, liveness.faceImage()).similarity();
            boolean verified = similarity >= configLoader.loadAdvanceAi().faceThreshold();
            Instant expiresAt = now.plus(TICKET_TTL);
            deviceSwitchFaceRepository.insert(new DeviceSwitchFaceVerificationRepository.FaceVerificationInsert(
                    token,
                    profile.userId(),
                    profile.partnerUserId(),
                    requestId,
                    deviceNo,
                    livenessId,
                    liveness.result(),
                    liveness.sequenceId(),
                    baseline.baselineType(),
                    baseline.faceEncryptedRef(),
                    candidateRef,
                    similarity,
                    verified ? "VERIFIED_PENDING" : "FAILED",
                    expiresAt
            ));
            if (!verified) {
                attemptLimiter.recordFailure(mobileNo, deviceNo);
                throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
            }
            attemptLimiter.clearFailures(mobileNo, deviceNo);
            return new FaceVerifyResult(
                    requestId,
                    true,
                    token,
                    FaceVerifyTicketSupport.expiresInSeconds(expiresAt, now)
            );
        } catch (ApiException exception) {
            if (exception.apiCode() != ApiCode.OCR_LIVENESS_FAILED
                    && exception.apiCode() != ApiCode.FACE_RECOGNITION_FAILED) {
                attemptLimiter.recordFailure(mobileNo, deviceNo);
            }
            throw exception;
        } finally {
            OcrCallContextHolder.clear();
        }
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return value.trim();
    }

    public record FaceVerifyCommand(String requestId, String livenessId, String deviceNo) {
    }

    public record FaceVerifyResult(String requestId, boolean verified, String faceVerifyToken, long expiresIn) {
    }
}
