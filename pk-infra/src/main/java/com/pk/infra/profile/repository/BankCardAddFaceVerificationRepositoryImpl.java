package com.pk.infra.profile.repository;

import com.pk.core.profile.port.BankCardAddFaceVerificationRepository;
import com.pk.infra.profile.mapper.BankCardAddFaceVerificationMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BankCardAddFaceVerificationRepositoryImpl implements BankCardAddFaceVerificationRepository {
    private final BankCardAddFaceVerificationMapper mapper;

    public BankCardAddFaceVerificationRepositoryImpl(BankCardAddFaceVerificationMapper mapper) {
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
    public boolean consume(String faceVerifyToken, Instant consumedAt) {
        return mapper.consume(faceVerifyToken, consumedAt) > 0;
    }
}
