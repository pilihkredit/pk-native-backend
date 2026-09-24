package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import com.pk.infra.auth.AuthDeviceNoNormalizer;
import java.time.Instant;
import java.util.Objects;

/** PRD: every add-bank-card attempt requires face verification before save. */
public class BankCardAddFaceGateService {
    private final BankCardAddFaceVerificationRepository bankCardAddFaceRepository;

    public BankCardAddFaceGateService(BankCardAddFaceVerificationRepository bankCardAddFaceRepository) {
        this.bankCardAddFaceRepository = bankCardAddFaceRepository;
    }

    public void assertSaveAllowed(long userId, String deviceNo, String faceVerifyToken) {
        String token = normalizeToken(faceVerifyToken);
        if (token == null) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        }
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        bankCardAddFaceRepository.findByToken(token)
                .filter(value -> value.userId() == userId)
                .filter(value -> Objects.equals(value.deviceNo(), normalizedDevice))
                .filter(value -> value.usableAt(Instant.now()))
                .orElseThrow(() -> new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED));
    }

    /** Consumes ticket before persisting bank card so save success cannot follow a failed consume. */
    public void consumeBeforeSave(long userId, String deviceNo, String faceVerifyToken) {
        assertSaveAllowed(userId, deviceNo, faceVerifyToken);
        String token = normalizeToken(faceVerifyToken);
        if (!bankCardAddFaceRepository.consume(token, Instant.now())) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        }
    }

    private static String normalizeToken(String faceVerifyToken) {
        if (faceVerifyToken == null || faceVerifyToken.isBlank()) {
            return null;
        }
        return faceVerifyToken.trim();
    }
}
