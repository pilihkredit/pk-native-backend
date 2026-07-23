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
    public Optional<ProfileBankCardData> findDefaultByProfileId(long profileId) {
        return Optional.ofNullable(profileBankCardMapper.findDefaultByProfileId(profileId)).map(this::toData);
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
    public void insert(ProfileBankCardData data) {
        profileBankCardMapper.insert(toRow(data));
    }

    @Override
    public void updateById(ProfileBankCardData data) {
        profileBankCardMapper.updateById(toRow(data));
    }

    @Override
    public void clearDefaultByProfileId(long profileId) {
        profileBankCardMapper.clearDefaultByProfileId(profileId);
    }

    @Override
    public void updateLastLenderAudit(String lastRequestId, String requestDataJson, String responseDataJson) {
        profileBankCardMapper.updateLastLenderAudit(lastRequestId, requestDataJson, responseDataJson);
    }

    private ProfileBankCardRow toRow(ProfileBankCardData data) {
        return new ProfileBankCardRow(
                data.id(),
                data.profileId(),
                data.mobileNo(),
                data.bankCode(),
                data.cardNumber().ciphertextBase64(),
                data.cardNumber().nonce(),
                data.cardNumber().tag(),
                data.cardNoHash(),
                data.verifyStatus(),
                data.verifyErrorCode(),
                data.defaultFlag(),
                data.moduleStatus(),
                data.lastRequestId(),
                data.lastLenderRequestJson(),
                data.lastLenderResponseJson()
        );
    }

    private ProfileBankCardData toData(ProfileBankCardRow row) {
        return new ProfileBankCardData(
                row.id(),
                row.profileId(),
                row.mobileNo(),
                row.bankCode(),
                new EncryptedField(row.cardNoCiphertext(), row.cardNoNonce(), row.cardNoTag()),
                row.cardNoHash(),
                row.verifyStatus(),
                row.verifyErrorCode(),
                row.defaultFlag(),
                row.moduleStatus(),
                row.lastRequestId(),
                row.lastLenderRequestJson(),
                row.lastLenderResponseJson()
        );
    }
}
