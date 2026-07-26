package com.pk.infra.auth.repository;

import com.pk.core.auth.port.UserMobileChangeLogRepository;
import com.pk.infra.auth.mapper.UserMobileChangeLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserMobileChangeLogRepositoryImpl implements UserMobileChangeLogRepository {
    private final UserMobileChangeLogMapper userMobileChangeLogMapper;

    public UserMobileChangeLogRepositoryImpl(UserMobileChangeLogMapper userMobileChangeLogMapper) {
        this.userMobileChangeLogMapper = userMobileChangeLogMapper;
    }

    @Override
    public void insert(UserMobileChangeLogEntry entry) {
        userMobileChangeLogMapper.insert(entry);
    }
}
