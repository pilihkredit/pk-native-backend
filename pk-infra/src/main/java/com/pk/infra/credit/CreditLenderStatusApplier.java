package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.time.Instant;
import java.util.Objects;

public class CreditLenderStatusApplier {
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;

    public CreditLenderStatusApplier(
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository
    ) {
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
    }

    public void apply(
            CreditApplicationRepository.CreditApplicationRecord record,
            LenderCreditPort.LenderCreditStatusResult status,
            String source,
            String limitSource
    ) {
        String previousExternal = creditLenderStatusQueryRepository.findByApplyId(record.applyId())
                .map(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData::externalStatus)
                .orElse(null);

        creditLenderStatusQueryRepository.upsert(new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                record.applyId(),
                record.profileId(),
                record.mobileNo(),
                record.partnerUserId(),
                status.lenderUserId(),
                status.creditApplyNo(),
                status.externalStatus(),
                status.creditContractExpireTime(),
                status.freezeEndTime(),
                status.riskMinLimit(),
                status.riskMaxLimit(),
                status.psychologicalCreditLimit(),
                status.fakeCreditLimit(),
                status.borrowAmtStepSize(),
                status.requestJson(),
                status.responseDataJson(),
                Instant.now()
        ));

        if (!hasExternalStatus(status.externalStatus())) {
            return;
        }

        String fromMapped = previousExternal == null
                ? null
                : CreditExternalStatusMapper.mapLenderStatus(previousExternal);
        String nextMapped = CreditExternalStatusMapper.mapLenderStatus(status.externalStatus());
        if (!Objects.equals(fromMapped, nextMapped)) {
            creditStatusHistoryRepository.insert(
                    record.id(),
                    record.mobileNo(),
                    fromMapped,
                    nextMapped,
                    status.externalStatus(),
                    source
            );
        }
    }

    private static boolean hasExternalStatus(String externalStatus) {
        return externalStatus != null && !externalStatus.isBlank();
    }
}
