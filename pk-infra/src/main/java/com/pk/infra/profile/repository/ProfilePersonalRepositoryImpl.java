package com.pk.infra.profile.repository;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.infra.profile.mapper.ProfilePersonalMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfilePersonalRepositoryImpl implements ProfilePersonalRepository {
    private final ProfilePersonalMapper mapper;

    public ProfilePersonalRepositoryImpl(ProfilePersonalMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ProfilePersonalData> findByUserId(long userId) {
        return Optional.ofNullable(mapper.findByUserId(userId)).map(this::toData);
    }

    @Override
    public void upsert(ProfilePersonalData data) {
        mapper.upsert(new ProfilePersonalRow(
                data.userId(),
                data.educationDegree(),
                data.industry(),
                data.income(),
                data.motherSurname().ciphertextBase64(),
                data.motherSurname().nonce(),
                data.motherSurname().tag(),
                data.userEmail(),
                data.moduleStatus(),
                data.lastRequestId(),
                data.externalInteractionId()
        ));
    }

    @Override
    public void updateEmail(long userId, String userEmail) {
        mapper.updateEmail(userId, userEmail);
    }

    @Override
    public void updateLastLenderInteraction(long userId, Long externalInteractionId) {
        mapper.updateLastLenderInteraction(userId, externalInteractionId);
    }

    private ProfilePersonalData toData(ProfilePersonalRow row) {
        return new ProfilePersonalData(
                row.userId(),
                row.educationDegree(),
                row.industry(),
                row.income(),
                new EncryptedField(row.motherSurnameCiphertext(), row.motherSurnameNonce(), row.motherSurnameTag()),
                row.userEmail(),
                row.moduleStatus(),
                row.lastRequestId(),
                row.externalInteractionId()
        );
    }
}
