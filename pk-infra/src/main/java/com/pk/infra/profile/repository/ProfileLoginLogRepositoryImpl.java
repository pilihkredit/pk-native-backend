package com.pk.infra.profile.repository;

import com.pk.core.profile.ProfileLoginLogData;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.infra.profile.mapper.ProfileLoginLogMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileLoginLogRepositoryImpl implements ProfileLoginLogRepository {
    private final ProfileLoginLogMapper profileLoginLogMapper;

    public ProfileLoginLogRepositoryImpl(ProfileLoginLogMapper profileLoginLogMapper) {
        this.profileLoginLogMapper = profileLoginLogMapper;
    }

    @Override
    public Optional<ProfileLoginLogData> findByProfileId(long profileId) {
        return Optional.ofNullable(profileLoginLogMapper.findByProfileId(profileId)).map(this::toData);
    }

    @Override
    public void upsert(ProfileLoginLogData data) {
        profileLoginLogMapper.upsert(new ProfileLoginLogRow(
                data.profileId(),
                data.mobileNo(),
                data.loginType(),
                data.loginIp(),
                data.loginLat(),
                data.loginLng(),
                data.moduleStatus(),
                data.lastRequestId(),
                data.externalInteractionId()
        ));
    }

    @Override
    public void updateLastLenderInteraction(long profileId, Long externalInteractionId) {
        profileLoginLogMapper.updateLastLenderInteraction(profileId, externalInteractionId);
    }

    private ProfileLoginLogData toData(ProfileLoginLogRow row) {
        return new ProfileLoginLogData(
                row.profileId(),
                row.mobileNo(),
                row.loginType(),
                row.loginIp(),
                row.loginLat(),
                row.loginLng(),
                row.moduleStatus(),
                row.lastRequestId(),
                row.externalInteractionId()
        );
    }
}
