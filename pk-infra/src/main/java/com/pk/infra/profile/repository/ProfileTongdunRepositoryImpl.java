package com.pk.infra.profile.repository;

import com.pk.core.profile.ProfileTongdunData;
import com.pk.core.profile.port.ProfileTongdunRepository;
import com.pk.infra.profile.mapper.ProfileTongdunMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileTongdunRepositoryImpl implements ProfileTongdunRepository {
    private final ProfileTongdunMapper profileTongdunMapper;

    public ProfileTongdunRepositoryImpl(ProfileTongdunMapper profileTongdunMapper) {
        this.profileTongdunMapper = profileTongdunMapper;
    }

    @Override
    public Optional<ProfileTongdunData> findByRequestId(String requestId) {
        return Optional.ofNullable(profileTongdunMapper.findByRequestId(requestId)).map(this::toData);
    }

    @Override
    public long insert(ProfileTongdunData data) {
        ProfileTongdunRow row = toRow(data);
        profileTongdunMapper.insert(row);
        return row.id == null ? 0L : row.id;
    }

    @Override
    public void updateLastLenderInteraction(String requestId, Long externalInteractionId) {
        profileTongdunMapper.updateLastLenderInteraction(requestId, externalInteractionId);
    }

    private ProfileTongdunData toData(ProfileTongdunRow row) {
        return new ProfileTongdunData(
                row.id,
                row.profileId,
                row.mobileNo,
                row.sceneType,
                row.tongdunKey,
                row.moduleStatus,
                row.requestId,
                row.externalInteractionId
        );
    }

    private static ProfileTongdunRow toRow(ProfileTongdunData data) {
        ProfileTongdunRow row = new ProfileTongdunRow();
        row.profileId = data.profileId();
        row.mobileNo = data.mobileNo();
        row.sceneType = data.sceneType();
        row.tongdunKey = data.tongdunKey();
        row.moduleStatus = data.moduleStatus();
        row.requestId = data.requestId();
        return row;
    }
}
