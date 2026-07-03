package com.pk.infra.loan;

import com.pk.core.api.ApiException;
import com.pk.core.api.ApiCode;
import com.pk.core.loan.port.ContractFileRepository;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import java.time.Instant;
import java.util.List;

public class LoanContractFacade {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LenderLoanContractPort lenderLoanContractPort;
    private final ContractFileRepository contractFileRepository;

    public LoanContractFacade(
            LoanApplicationRepository loanApplicationRepository,
            LenderLoanContractPort lenderLoanContractPort,
            ContractFileRepository contractFileRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.lenderLoanContractPort = lenderLoanContractPort;
        this.contractFileRepository = contractFileRepository;
    }

    public ContractsResult listContracts(long profileId, String loanApplyId) {
        LoanApplicationRepository.LoanApplicationRecord record = loanApplicationRepository
                .findByLoanApplyIdAndProfileId(loanApplyId, profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));

        LenderLoanContractPort.LenderLoanContractListResult lenderResult =
                lenderLoanContractPort.listContracts(loanApplyId);
        Instant fetchedAt = Instant.now();
        for (LenderLoanContractPort.LenderLoanContract contract : lenderResult.contracts()) {
            contractFileRepository.upsert(new ContractFileRepository.ContractFileUpsert(
                    record.id(),
                    lenderResult.billNo(),
                    contract.contractType(),
                    contract.contractName(),
                    contract.contractUrl(),
                    lenderResult.requestJson(),
                    lenderResult.responseDataJson(),
                    fetchedAt
            ));
        }

        List<ContractFileRepository.ContractFileRecord> persisted =
                contractFileRepository.findByLoanApplicationId(record.id());
        return new ContractsResult(
                loanApplyId,
                persisted.stream().map(LoanContractFacade::toContract).toList()
        );
    }

    private static ContractResult toContract(ContractFileRepository.ContractFileRecord record) {
        return new ContractResult(
                record.contractType(),
                record.contractName(),
                record.contractUrl(),
                null
        );
    }

    public record ContractsResult(String loanApplyId, List<ContractResult> contracts) {
    }

    public record ContractResult(
            String contractType,
            String contractName,
            String contractUrl,
            String signStatus
    ) {
    }
}
