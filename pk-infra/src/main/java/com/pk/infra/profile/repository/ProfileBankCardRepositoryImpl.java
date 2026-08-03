package com.pk.infra.profile.repository;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.infra.profile.mapper.ProfileBankCardMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileBankCardRepositoryImpl implements ProfileBankCardRepository {
    private final ProfileBankCardMapper profileBankCardMapper;

    public ProfileBankCardRepositoryImpl(ProfileBankCardMapper profileBankCardMapper) {
        this.profileBankCardMapper = profileBankCardMapper;
    }

    @Override
    public Optional<ProfileBankCardData> findDefaultByUserId(long userId) {
        return Optional.ofNullable(profileBankCardMapper.findDefaultByUserId(userId)).map(this::toData);
    }

    @Override
    public Optional<ProfileBankCardData> findByLastRequestId(String lastRequestId) {
        return Optional.ofNullable(profileBankCardMapper.findByLastRequestId(lastRequestId)).map(this::toData);
    }

    @Override
    public Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash) {
        return Optional.ofNullable(profileBankCardMapper.findByCardNoHash(cardNoHash)).map(this::toData);
    }

    @Override
    public Optional<ProfileBankCardData> findActiveByUserIdAndCardNoHash(long userId, String cardNoHash) {
        return Optional.ofNullable(profileBankCardMapper.findActiveByUserIdAndCardNoHash(userId, cardNoHash))
                .map(this::toData);
    }

    @Override
    public int countActiveByUserId(long userId) {
        return profileBankCardMapper.countActiveByUserId(userId);
    }

    @Override
    public void insert(ProfileBankCardData data) {
        profileBankCardMapper.insert(toRow(data));
    }

    @Override
    public void updateById(ProfileBankCardData data) {
        profileBankCardMapper.updateById(toRow(data));
    }

    @Override
    public void clearDefaultByUserId(long userId) {
        profileBankCardMapper.clearDefaultByUserId(userId);
    }

    @Override
    public void setDefaultByUserIdAndCardId(long userId, long cardId) {
        profileBankCardMapper.setDefaultByUserIdAndCardId(userId, cardId);
    }

    @Override
    public void softDeleteById(long id, String lastRequestId) {
        profileBankCardMapper.softDeleteById(id, lastRequestId);
    }

    @Override
    public void updateLastLenderInteraction(String lastRequestId, Long externalInteractionId) {
        profileBankCardMapper.updateLastLenderInteraction(lastRequestId, externalInteractionId);
    }

    private ProfileBankCardRow toRow(ProfileBankCardData data) {
        return new ProfileBankCardRow(
                data.id(),
                data.userId(),
                data.bankCode(),
                data.cardNumber().ciphertextBase64(),
                data.cardNumber().nonce(),
                data.cardNumber().tag(),
                data.cardNoHash(),
                data.verifyStatus(),
                data.verifyErrorCode(),
                data.defaultFlag(),
                data.deletedFlag(),
                data.moduleStatus(),
                data.lastRequestId(),
                data.externalInteractionId()
        );
    }

    private ProfileBankCardData toData(ProfileBankCardRow row) {
        return new ProfileBankCardData(
                row.id(),
                row.userId(),
                row.bankCode(),
                new EncryptedField(row.cardNoCiphertext(), row.cardNoNonce(), row.cardNoTag()),
                row.cardNoHash(),
                row.verifyStatus(),
                row.verifyErrorCode(),
                row.defaultFlag(),
                row.deletedFlag(),
                row.moduleStatus(),
                row.lastRequestId(),
                row.externalInteractionId()
        );
    }
}
