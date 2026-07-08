package com.pk.infra.attribution.repository;

import com.pk.core.attribution.port.AdjustEventConfigRepository;
import com.pk.infra.attribution.mapper.AdjustEventConfigMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AdjustEventConfigRepositoryImpl implements AdjustEventConfigRepository {
    private final AdjustEventConfigMapper mapper;

    public AdjustEventConfigRepositoryImpl(AdjustEventConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<AdjustEventConfigData> findEnabledByEventNameAndAppToken(String eventName, String appToken) {
        return Optional.ofNullable(mapper.findEnabledByEventNameAndAppToken(eventName, appToken));
    }
}
