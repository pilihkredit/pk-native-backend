package com.pk.core.reference.port;

import com.pk.core.reference.BankReference;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefBankRepository {
    List<BankReference> findAllActive();

    Optional<Instant> findLatestSyncedAt();

    boolean existsActiveBankCode(String bankCode);

    void replaceAll(List<BankReference> banks, Instant syncedAt);
}
