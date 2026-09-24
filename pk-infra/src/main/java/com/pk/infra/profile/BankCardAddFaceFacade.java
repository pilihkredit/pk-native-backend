package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.auth.AuthDeviceNoNormalizer;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class BankCardAddFaceFacade {
    private static final Duration TICKET_TTL = Duration.ofMinutes(5);

    private final TrustDecisionKycPort trustDecisionKycPort;
    private final AdvanceAiOcrPort advanceAiOcrPort;
    private final FaceComparisonBaselineResolver baselineResolver;
    private final BankCardAddFaceVerificationRepository bankCardAddFaceRepository;
    private final BiometricImageStore biometricImageStore;
    private final OcrProviderConfigLoader configLoader;

    public BankCardAddFaceFacade(
            TrustDecisionKycPort trustDecisionKycPort,
            AdvanceAiOcrPort advanceAiOcrPort,
            FaceComparisonBaselineResolver baselineResolver,
            BankCardAddFaceVerificationRepository bankCardAddFaceRepository,
            BiometricImageStore biometricImageStore,
            OcrProviderConfigLoader configLoader
    ) {
        this.trustDecisionKycPort = trustDecisionKycPort;
        this.advanceAiOcrPort = advanceAiOcrPort;
        this.baselineResolver = baselineResolver;
        this.bankCardAddFaceRepository = bankCardAddFaceRepository;
        this.biometricImageStore = biometricImageStore;
        this.configLoader = configLoader;
    }

    public FaceVerifyResult verify(
            long userId,
            String partnerUserId,
            String mobileNo,
            FaceVerifyCommand command
    ) {
        String requestId = requireText(command.requestId());
        String livenessId = requireText(command.livenessId());
        String deviceNo = AuthDeviceNoNormalizer.requireNonBlank(command.deviceNo());

        Instant now = Instant.now();
        FaceComparisonBaseline baseline = baselineResolver.resolveOrThrow(userId);
        byte[] baselineImage = biometricImageStore.load(baseline.faceEncryptedRef());
        TrustDecisionKycPort.SdkLivenessResult liveness =
                trustDecisionKycPort.retrieveLivenessResult(livenessId);
        if (liveness.faceImage() == null || liveness.faceImage().length == 0
                || "fail".equalsIgnoreCase(liveness.result())) {
            throw new ApiException(ApiCode.OCR_LIVENESS_FAILED);
        }

        String token = UUID.randomUUID().toString();
        String candidateRef = biometricImageStore.storeVersioned(
                mobileNo,
                BiometricImageKind.BANK_CARD_ADD_FACE,
                token,
                liveness.faceImage()
        );
        double similarity = advanceAiOcrPort.compareFaces(baselineImage, liveness.faceImage()).similarity();
        boolean verified = similarity >= configLoader.loadAdvanceAi().faceThreshold();
        Instant expiresAt = now.plus(TICKET_TTL);
        bankCardAddFaceRepository.insert(new BankCardAddFaceVerificationRepository.FaceVerificationInsert(
                token,
                userId,
                partnerUserId,
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
            throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
        }
        return new FaceVerifyResult(
                requestId,
                true,
                token,
                FaceVerifyTicketSupport.expiresInSeconds(expiresAt, now)
        );
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
