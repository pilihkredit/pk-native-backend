package com.pk.core.provider.port;

import java.util.Optional;

public interface PkProviderRepository {
    Optional<String> findActiveProviderCode(String providerCode);
}
