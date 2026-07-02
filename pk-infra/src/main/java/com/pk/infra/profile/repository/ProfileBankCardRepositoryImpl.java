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
    public ProfileBankCardRepositoryImpl(ProfileBankCardMapper profileBankCardMapper) { this.profileBankCardMapper = profileBankCardMapper; }
    @Override public Optional<ProfileBankCardData> findByProfileId(long profileId) {
        return Optional.ofNullable(profileBankCardMapper.findByProfileId(profileId)).map(this::toData);
    }
    @Override public Optional<ProfileBankCardData> findByCardNoHash(String cardNoHash) {
        return Optional.ofNullable(profileBankCardMapper.findByCardNoHash(cardNoHash)).map(this::toData);
    }
    @Override public void upsert(ProfileBankCardData data) {
        profileBankCardMapper.upsert(new ProfileBankCardRow(data.profileId(), data.mobileNo(), data.bankCode(),
                data.cardNumber().ciphertextBase64(), data.cardNumber().nonce(), data.cardNumber().tag(),
                data.cardNoHash(), data.verifyStatus(), data.verifyErrorCode(), data.moduleStatus(), data.lastRequestId(), null, null));
    }
    @Override public void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson) {
        profileBankCardMapper.updateLastLenderAudit(profileId, requestDataJson, responseDataJson);
    }
    private ProfileBankCardData toData(ProfileBankCardRow row) {
        return new ProfileBankCardData(row.profileId(), row.mobileNo(), row.bankCode(),
                new EncryptedField(row.cardNoCiphertext(), row.cardNoNonce(), row.cardNoTag()),
                row.cardNoHash(), row.verifyStatus(), row.verifyErrorCode(), row.moduleStatus(),
                row.lastRequestId(), row.lastLenderRequestJson(), row.lastLenderResponseJson());
    }
}
