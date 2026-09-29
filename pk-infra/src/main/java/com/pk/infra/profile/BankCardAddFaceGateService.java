package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import com.pk.infra.auth.AuthDeviceNoNormalizer;
import java.time.Instant;
import java.util.Objects;

/**
 * Face ticket for add-bank-card (2nd+ card): one successful lender bind per verify session.
 * Failed saves (validation, KTA reject) do not consume the token; user may retry with the same token
 * until {@link #consumeAfterSuccessfulAdd} runs or the ticket expires.
 */
public class BankCardAddFaceGateService {
    private final BankCardAddFaceVerificationRepository bankCardAddFaceRepository;

    public BankCardAddFaceGateService(BankCardAddFaceVerificationRepository bankCardAddFaceRepository) {
        this.bankCardAddFaceRepository = bankCardAddFaceRepository;
    }

    public void assertSaveAllowed(long userId, String deviceNo, String faceVerifyToken) {
        resolveUsableTicket(userId, deviceNo, faceVerifyToken);
    }

    private BankCardAddFaceVerificationRepository.FaceVerificationData resolveUsableTicket(
            long userId,
            String deviceNo,
            String faceVerifyToken
    ) {
        String token = normalizeToken(faceVerifyToken);
        if (token == null) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        }
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        Instant now = Instant.now();
        BankCardAddFaceVerificationRepository.FaceVerificationData ticket = bankCardAddFaceRepository
                .findByToken(token)
                .orElseThrow(() -> new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED));
        if (ticket.userId() != userId || !Objects.equals(ticket.deviceNo(), normalizedDevice)) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        }
        if (isExpiredPending(ticket, now)) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_EXPIRED);
        }
        if (!ticket.usableAt(now)) {
            throw new ApiException(ApiCode.BANK_CARD_FACE_VERIFICATION_REQUIRED);
        }
        return ticket;
    }

    private static boolean isExpiredPending(
            BankCardAddFaceVerificationRepository.FaceVerificationData ticket,
            Instant now
    ) {
        return "VERIFIED_PENDING".equals(ticket.status())
                && ticket.expiresAt() != null
                && !ticket.expiresAt().isAfter(now);
    }

    /** Marks ticket CONSUMED after one bank card is successfully synced to the lender. */
    public void consumeAfterSuccessfulAdd(long userId, String deviceNo, String faceVerifyToken) {
        resolveUsableTicket(userId, deviceNo, faceVerifyToken);
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
