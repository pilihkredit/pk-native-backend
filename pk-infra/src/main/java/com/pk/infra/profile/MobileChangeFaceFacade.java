package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class MobileChangeFaceFacade {
    private static final Duration TICKET_TTL = Duration.ofMinutes(5);

    private final TrustDecisionKycPort trustDecisionKycPort;
    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final MobileChangeFaceVerificationRepository verificationRepository;
    private final BiometricImageStore biometricImageStore;
    private final OcrProviderConfigLoader configLoader;

    public MobileChangeFaceFacade(
            TrustDecisionKycPort trustDecisionKycPort,
            AdvanceAiOcrPort advanceAiOcrPort,
            ProfileIdentityRepository profileIdentityRepository,
            MobileChangeFaceVerificationRepository verificationRepository,
            BiometricImageStore biometricImageStore,
            OcrProviderConfigLoader configLoader
    ) {
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.profileIdentityRepository = profileIdentityRepository;
        this.verificationRepository = verificationRepository;
        this.biometricImageStore = biometricImageStore;
        this.configLoader = configLoader;
    }

    public FaceVerifyResult verify(
            long userId,
            String partnerUserId,
            String mobileNo,
            FaceVerifyCommand command
    ) {
        requireText(command.requestId());
        requireText(command.livenessId());
        requireText(command.deviceNo());

        Baseline baseline = resolveBaseline(userId);
        byte[] baselineImage = biometricImageStore.load(baseline.encryptedRef());
        TrustDecisionKycPort.SdkLivenessResult liveness =
                trustDecisionKycPort.retrieveLivenessResult(command.livenessId().trim());
        if (liveness.faceImage() == null || liveness.faceImage().length == 0
                || "fail".equalsIgnoreCase(liveness.result())) {
            throw new ApiException(ApiCode.OCR_LIVENESS_FAILED);
        }

        String token = UUID.randomUUID().toString();
        String candidateRef = biometricImageStore.storeVersioned(
                mobileNo,
                BiometricImageKind.MOBILE_CHANGE_FACE,
                token,
                liveness.faceImage()
        );
        double similarity = advanceAiOcrPort.compareFaces(baselineImage, liveness.faceImage()).similarity();
        boolean verified = similarity >= configLoader.loadAdvanceAi().faceThreshold();
        Instant expiresAt = Instant.now().plus(TICKET_TTL);
        verificationRepository.insert(new MobileChangeFaceVerificationRepository.FaceVerificationInsert(
                token,
                userId,
                partnerUserId,
                command.requestId().trim(),
                command.deviceNo().trim(),
                command.livenessId().trim(),
                liveness.result(),
                liveness.sequenceId(),
                baseline.type(),
                baseline.encryptedRef(),
                candidateRef,
                similarity,
                verified ? "VERIFIED_PENDING" : "FAILED",
                expiresAt
        ));
        if (!verified) {
            throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
        }
        return new FaceVerifyResult(command.requestId().trim(), true, token, TICKET_TTL.toSeconds());
    }

    private Baseline resolveBaseline(long userId) {
        return verificationRepository.findLatestPromotedByUserId(userId)
                .map(value -> new Baseline("PREVIOUS_MOBILE_CHANGE", value.candidateFaceEncryptedRef()))
                .orElseGet(() -> {
                    ProfileIdentityData identity = profileIdentityRepository.findByUserId(userId)
                            .orElseThrow(() -> new ApiException(ApiCode.FACE_RECOGNITION_FAILED));
                    if (identity.facePhotoImageEncryptedRef() == null
                            || identity.facePhotoImageEncryptedRef().isBlank()) {
                        throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
                    }
                    return new Baseline("IDENTITY", identity.facePhotoImageEncryptedRef());
                });
    }

    private static void requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private record Baseline(String type, String encryptedRef) {
    }

    public record FaceVerifyCommand(String requestId, String livenessId, String deviceNo, String traceId) {
    }

    public record FaceVerifyResult(String requestId, boolean verified, String faceVerifyToken, long expiresIn) {
    }
}
