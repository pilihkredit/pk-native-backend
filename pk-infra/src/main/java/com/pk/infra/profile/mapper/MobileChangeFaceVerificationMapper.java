package com.pk.infra.profile.mapper;

import com.pk.core.profile.port.MobileChangeFaceVerificationRepository.FaceVerificationData;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository.FaceVerificationInsert;
import java.time.Instant;
import org.apache.ibatis.annotations.Param;

public interface MobileChangeFaceVerificationMapper {
    int insert(FaceVerificationInsert insert);

    FaceVerificationData findLatestPromotedByUserId(@Param("userId") long userId);

    FaceVerificationData findVerifiedByToken(@Param("faceVerifyToken") String faceVerifyToken);

    int attachOtpToken(
            @Param("faceVerifyToken") String faceVerifyToken,
            @Param("otpToken") String otpToken
    );

    int promote(
            @Param("faceVerifyToken") String faceVerifyToken,
            @Param("promotedAt") Instant promotedAt
    );
}
