package com.pk.core.loan.port;

import java.time.Instant;
import java.util.List;

public interface ContractFileRepository {
    void upsert(ContractFileUpsert upsert);

    List<ContractFileRecord> findByLoanApplicationId(long loanApplicationId);

    record ContractFileUpsert(
            long loanApplicationId,
            String billNo,
            String contractType,
            String contractName,
            String contractUrl,
            Long externalInteractionId,
            Instant fetchedAt
    ) {
    }

    record ContractFileRecord(
            long id,
            long loanApplicationId,
            String billNo,
            String contractType,
            String contractName,
            String contractUrl,
            Long externalInteractionId,
            Instant fetchedAt
    ) {
    }
}
