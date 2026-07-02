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
    public ProfilePersonalRepositoryImpl(ProfilePersonalMapper mapper) { this.mapper = mapper; }
    @Override public Optional<ProfilePersonalData> findByProfileId(long profileId) {
        return Optional.ofNullable(mapper.findByProfileId(profileId)).map(this::toData);
    }
    @Override public void upsert(ProfilePersonalData data) {
        mapper.upsert(new ProfilePersonalRow(data.profileId(), data.mobileNo(), data.educationDegree(), data.industry(),
                data.income(), data.motherSurname().ciphertextBase64(), data.motherSurname().nonce(), data.motherSurname().tag(),
                data.userEmail(), data.moduleStatus(), data.lastRequestId(), null, null));
    }
    @Override public void updateEmail(long profileId, String userEmail) { mapper.updateEmail(profileId, userEmail); }
    @Override public void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson) {
        mapper.updateLastLenderAudit(profileId, requestDataJson, responseDataJson);
    }
    private ProfilePersonalData toData(ProfilePersonalRow row) {
        return new ProfilePersonalData(row.profileId(), row.mobileNo(), row.educationDegree(), row.industry(), row.income(),
                new EncryptedField(row.motherSurnameCiphertext(), row.motherSurnameNonce(), row.motherSurnameTag()),
                row.userEmail(), row.moduleStatus(), row.lastRequestId(), row.lastLenderRequestJson(), row.lastLenderResponseJson());
    }
}
