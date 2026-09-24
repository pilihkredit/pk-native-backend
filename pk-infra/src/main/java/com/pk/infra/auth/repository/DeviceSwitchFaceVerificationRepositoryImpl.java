package com.pk.infra.auth.repository;

import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.infra.auth.mapper.DeviceSwitchFaceVerificationMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceSwitchFaceVerificationRepositoryImpl implements DeviceSwitchFaceVerificationRepository {
    private final DeviceSwitchFaceVerificationMapper mapper;

    public DeviceSwitchFaceVerificationRepositoryImpl(DeviceSwitchFaceVerificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(FaceVerificationInsert insert) {
        mapper.insert(insert);
    }

    @Override
    public Optional<FaceVerificationData> findByToken(String faceVerifyToken) {
        return Optional.ofNullable(mapper.findByToken(faceVerifyToken));
    }

    @Override
    public Optional<FaceVerificationData> findReusablePendingByUserIdAndRequestId(long userId, String requestId) {
        return Optional.ofNullable(mapper.findReusablePendingByUserIdAndRequestId(userId, requestId));
    }

    @Override
    public boolean consume(String faceVerifyToken, Instant consumedAt) {
        return mapper.consume(faceVerifyToken, consumedAt) > 0;
    }
}
