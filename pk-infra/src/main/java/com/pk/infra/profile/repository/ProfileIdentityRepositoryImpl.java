package com.pk.infra.profile.repository;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.infra.profile.mapper.ProfileIdentityMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileIdentityRepositoryImpl implements ProfileIdentityRepository {
    private final ProfileIdentityMapper profileIdentityMapper;

    public ProfileIdentityRepositoryImpl(ProfileIdentityMapper profileIdentityMapper) {
        this.profileIdentityMapper = profileIdentityMapper;
    }

    @Override
    public Optional<ProfileIdentityData> findByProfileId(long profileId) {
        return Optional.ofNullable(profileIdentityMapper.findByProfileId(profileId)).map(this::toData);
    }

    @Override
    public void upsert(ProfileIdentityData data) {
        profileIdentityMapper.upsert(new ProfileIdentityRow(
                data.profileId(),
                data.mobileNo(),
                data.fullName(),
                data.idNo().ciphertextBase64(),
                data.idNo().nonce(),
                data.idNo().tag(),
                data.idNoHash(),
                data.moduleStatus(),
                data.lastRequestId(),
                null,
                null
        ));
    }

    @Override
    public void updateLastLenderAudit(long profileId, String requestJson, String responseJson) {
        profileIdentityMapper.updateLastLenderAudit(profileId, requestJson, responseJson);
    }

    private ProfileIdentityData toData(ProfileIdentityRow row) {
        return new ProfileIdentityData(
                row.profileId(),
                row.mobileNo(),
                row.fullName(),
                new EncryptedField(row.idNoCiphertext(), row.idNoNonce(), row.idNoTag()),
                row.idNoHash(),
                row.moduleStatus(),
                row.lastRequestId(),
                row.lastLenderRequestJson(),
                row.lastLenderResponseJson()
        );
    }
}
