package com.pk.infra.appconfig.repository;

import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.infra.appconfig.mapper.AppConfigMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AppConfigRepositoryImpl implements AppConfigRepository {
    private final AppConfigMapper mapper;

    public AppConfigRepositoryImpl(AppConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<AppConfigRecord> findByKey(String key) {
        return Optional.ofNullable(mapper.findByKey(key));
    }
}
