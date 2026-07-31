package com.pk.infra.provider.repository;

import com.pk.core.provider.port.PkProviderRepository;
import com.pk.infra.provider.mapper.PkProviderMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PkProviderRepositoryImpl implements PkProviderRepository {
    private final PkProviderMapper pkProviderMapper;

    public PkProviderRepositoryImpl(PkProviderMapper pkProviderMapper) {
        this.pkProviderMapper = pkProviderMapper;
    }

    @Override
    public Optional<String> findActiveProviderCode(String providerCode) {
        return Optional.ofNullable(pkProviderMapper.findActiveProviderCode(providerCode));
    }
}
