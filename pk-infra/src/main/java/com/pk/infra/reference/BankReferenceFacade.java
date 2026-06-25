package com.pk.infra.reference;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.reference.BankReference;
import com.pk.core.reference.port.LenderBankPort;
import com.pk.core.reference.port.RefBankRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class BankReferenceFacade {
    private final RefBankRepository refBankRepository;
    private final LenderBankPort lenderBankPort;
    private final Duration bankCacheTtl;

    public BankReferenceFacade(
            RefBankRepository refBankRepository,
            LenderBankPort lenderBankPort,
            ReferenceProperties referenceProperties
    ) {
        this.refBankRepository = refBankRepository;
        this.lenderBankPort = lenderBankPort;
        this.bankCacheTtl = referenceProperties.bankCacheTtl();
    }

    public List<BankReference> listBanks() {
        List<BankReference> cached = refBankRepository.findAllActive();
        if (!cached.isEmpty() && !isStale(refBankRepository.findLatestSyncedAt())) {
            return cached;
        }
        syncFromLender();
        return refBankRepository.findAllActive();
    }

    public boolean isValidBankCode(String bankCode) {
        if (bankCode == null || bankCode.isBlank()) {
            return false;
        }
        if (!refBankRepository.existsActiveBankCode(bankCode.trim())) {
            List<BankReference> banks = refBankRepository.findAllActive();
            if (banks.isEmpty() || isStale(refBankRepository.findLatestSyncedAt())) {
                syncFromLender();
            }
            return refBankRepository.existsActiveBankCode(bankCode.trim());
        }
        return true;
    }

    private void syncFromLender() {
        try {
            List<BankReference> banks = lenderBankPort.listBanks();
            if (banks == null || banks.isEmpty()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
            refBankRepository.replaceAll(banks, Instant.now());
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    public void refreshFromLender() {
        syncFromLender();
    }

    private boolean isStale(java.util.Optional<Instant> latestSyncedAt) {
        if (latestSyncedAt.isEmpty()) {
            return true;
        }
        return latestSyncedAt.get().plus(bankCacheTtl).isBefore(Instant.now());
    }
}
