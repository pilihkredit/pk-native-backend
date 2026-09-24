package com.pk.core.auth.port;

import java.time.Instant;
import java.util.Optional;

public interface DeviceSwitchFaceVerificationRepository {
    void insert(FaceVerificationInsert insert);

    Optional<FaceVerificationData> findByToken(String faceVerifyToken);

    Optional<FaceVerificationData> findReusablePendingByUserIdAndRequestId(long userId, String requestId);

    boolean consume(String faceVerifyToken, Instant consumedAt);

    record FaceVerificationInsert(
            String faceVerifyToken,
            long userId,
            String partnerUserId,
            String requestId,
            String deviceNo,
            String livenessId,
            String livenessResult,
            String livenessSequenceId,
            String baselineType,
            String baselineFaceEncryptedRef,
            String candidateFaceEncryptedRef,
            Double similarity,
            String status,
            Instant expiresAt
    ) {
    }

    record FaceVerificationData(
            long id,
            String faceVerifyToken,
            long userId,
            String partnerUserId,
            String requestId,
            String deviceNo,
            String livenessId,
            String baselineType,
            Double similarity,
            String status,
            Instant expiresAt,
            Instant consumedAt
    ) {
        public boolean usableAt(Instant now) {
            return "VERIFIED_PENDING".equals(status) && expiresAt != null && expiresAt.isAfter(now);
        }
    }
}
