package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.auth.FaceVerifyTicketTtlConfigLoader;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class MobileChangeFaceFacade {
    private final FaceVerifyTicketTtlConfigLoader ticketTtlLoader;
    private final TrustDecisionKycPort trustDecisionKycPort;
    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final FaceComparisonBaselineResolver baselineResolver;
    private final MobileChangeFaceVerificationRepository verificationRepository;
    private final BiometricImageStore biometricImageStore;
    private final OcrProviderConfigLoader configLoader;

    public MobileChangeFaceFacade(
            FaceVerifyTicketTtlConfigLoader ticketTtlLoader,
            TrustDecisionKycPort trustDecisionKycPort,
            AdvanceAiOcrPort advanceAiOcrPort,
            FaceComparisonBaselineResolver baselineResolver,
            MobileChangeFaceVerificationRepository verificationRepository,
            BiometricImageStore biometricImageStore,
            OcrProviderConfigLoader configLoader
    ) {
        this.ticketTtlLoader = ticketTtlLoader;
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.baselineResolver = baselineResolver;
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

        FaceComparisonBaseline baseline = baselineResolver.resolveOrThrow(userId);
        byte[] baselineImage = biometricImageStore.load(baseline.faceEncryptedRef());
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
        Instant now = Instant.now();
        Duration ticketTtl = ticketTtlLoader.mobileChangeTtl();
        Instant expiresAt = now.plus(ticketTtl);
        verificationRepository.insert(new MobileChangeFaceVerificationRepository.FaceVerificationInsert(
                token,
                userId,
                partnerUserId,
                command.requestId().trim(),
                command.deviceNo().trim(),
                command.livenessId().trim(),
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
            throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
        }
        return new FaceVerifyResult(
                command.requestId().trim(),
                true,
                token,
                FaceVerifyTicketSupport.expiresInSeconds(expiresAt, now)
        );
    }

    private static void requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    public record FaceVerifyCommand(String requestId, String livenessId, String deviceNo, String traceId) {
    }

    public record FaceVerifyResult(String requestId, boolean verified, String faceVerifyToken, long expiresIn) {
    }
}
