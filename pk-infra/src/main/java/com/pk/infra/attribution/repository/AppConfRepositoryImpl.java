package com.pk.infra.attribution.repository;

import com.pk.core.attribution.port.AppConfRepository;
import com.pk.infra.attribution.mapper.AppConfMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AppConfRepositoryImpl implements AppConfRepository {
    private final AppConfMapper mapper;

    public AppConfRepositoryImpl(AppConfMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<String> findConfigValue(String configKey) {
        return Optional.ofNullable(mapper.findConfigValue(configKey));
    }
}
