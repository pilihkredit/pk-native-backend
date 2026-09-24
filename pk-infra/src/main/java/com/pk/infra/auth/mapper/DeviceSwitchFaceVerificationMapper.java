package com.pk.infra.auth.mapper;

import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceSwitchFaceVerificationMapper {
    int insert(DeviceSwitchFaceVerificationRepository.FaceVerificationInsert insert);

    DeviceSwitchFaceVerificationRepository.FaceVerificationData findByToken(
            @Param("faceVerifyToken") String faceVerifyToken
    );

    DeviceSwitchFaceVerificationRepository.FaceVerificationData findReusablePendingByUserIdAndRequestId(
            @Param("userId") long userId,
            @Param("requestId") String requestId
    );

    int consume(
            @Param("faceVerifyToken") String faceVerifyToken,
            @Param("consumedAt") Instant consumedAt
    );
}
