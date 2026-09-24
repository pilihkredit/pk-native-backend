package com.pk.infra.profile.mapper;

import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BankCardAddFaceVerificationMapper {
    int insert(BankCardAddFaceVerificationRepository.FaceVerificationInsert insert);

    BankCardAddFaceVerificationRepository.FaceVerificationData findByToken(
            @Param("faceVerifyToken") String faceVerifyToken
    );

    int consume(
            @Param("faceVerifyToken") String faceVerifyToken,
            @Param("consumedAt") Instant consumedAt
    );
}
