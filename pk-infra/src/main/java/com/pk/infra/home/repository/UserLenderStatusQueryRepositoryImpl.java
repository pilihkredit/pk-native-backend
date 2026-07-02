package com.pk.infra.home.repository;

import com.pk.core.home.port.UserLenderStatusQueryRepository;
import com.pk.infra.home.mapper.UserLenderStatusQueryMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserLenderStatusQueryRepositoryImpl implements UserLenderStatusQueryRepository {
    private final UserLenderStatusQueryMapper mapper;

    public UserLenderStatusQueryRepositoryImpl(UserLenderStatusQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void upsert(UserLenderStatusQueryData data) {
        mapper.upsert(data);
    }

    @Override
    public Optional<UserLenderStatusQueryData> findByProfileId(long profileId) {
        return Optional.ofNullable(mapper.findByProfileId(profileId));
    }
}
