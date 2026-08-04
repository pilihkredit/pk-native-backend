package com.pk.infra.profile.repository;

import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.infra.profile.mapper.MobileChangeFaceVerificationMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MobileChangeFaceVerificationRepositoryImpl implements MobileChangeFaceVerificationRepository {
    private final MobileChangeFaceVerificationMapper mapper;

    public MobileChangeFaceVerificationRepositoryImpl(MobileChangeFaceVerificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(FaceVerificationInsert insert) {
        mapper.insert(insert);
    }

    @Override
    public Optional<FaceVerificationData> findLatestPromotedByUserId(long userId) {
        return Optional.ofNullable(mapper.findLatestPromotedByUserId(userId));
    }

    @Override
    public Optional<FaceVerificationData> findVerifiedByToken(String faceVerifyToken) {
        return Optional.ofNullable(mapper.findVerifiedByToken(faceVerifyToken));
    }

    @Override
    public void attachOtpToken(String faceVerifyToken, String otpToken) {
        mapper.attachOtpToken(faceVerifyToken, otpToken);
    }

    @Override
    public boolean promote(String faceVerifyToken, Instant promotedAt) {
        return mapper.promote(faceVerifyToken, promotedAt) == 1;
    }
}
