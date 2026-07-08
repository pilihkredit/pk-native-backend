package com.pk.infra.attribution.repository;

import com.pk.core.attribution.port.AdjustConfigRepository;
import com.pk.infra.attribution.mapper.AdjustConfigMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AdjustConfigRepositoryImpl implements AdjustConfigRepository {
    private final AdjustConfigMapper mapper;

    public AdjustConfigRepositoryImpl(AdjustConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<AdjustConfigData> findActiveByOsName(String osName) {
        return Optional.ofNullable(mapper.findActiveByOsName(osName));
    }
}
