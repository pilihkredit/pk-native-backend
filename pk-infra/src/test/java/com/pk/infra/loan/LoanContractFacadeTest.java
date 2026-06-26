package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.port.ContractFileRepository;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanContractFacadeTest {
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LenderLoanContractPort lenderLoanContractPort;
    @Mock
    private ContractFileRepository contractFileRepository;

    private LoanContractFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanContractFacade(
                loanApplicationRepository,
                lenderLoanContractPort,
                contractFileRepository
        );
    }

    @Test
    void listsContractsFromLenderAndPersists() {
        when(loanApplicationRepository.findByLoanApplyIdAndProfileId("LOAN-1", 1L))
                .thenReturn(Optional.of(loanApplication(10L)));
        when(lenderLoanContractPort.listContracts("LOAN-1")).thenReturn(
                new LenderLoanContractPort.LenderLoanContractListResult(
                        "LOAN-1",
                        "LN-1",
                        "BILL-1",
                        List.of(new LenderLoanContractPort.LenderLoanContract(
                                "LOAN_AGREEMENT",
                                "Loan Agreement",
                                "https://example.com/contract.pdf"
                        ))
                )
        );
        when(contractFileRepository.findByLoanApplicationId(10L)).thenReturn(List.of(
                new ContractFileRepository.ContractFileRecord(
                        1L,
                        10L,
                        "BILL-1",
                        "LOAN_AGREEMENT",
                        "Loan Agreement",
                        "https://example.com/contract.pdf",
                        Instant.parse("2026-06-24T00:00:00Z")
                )
        ));

        LoanContractFacade.ContractsResult result = facade.listContracts(1L, "LOAN-1");

        assertThat(result.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.contracts()).hasSize(1);
        assertThat(result.contracts().getFirst().contractNo()).isEqualTo("LOAN_AGREEMENT");
        assertThat(result.contracts().getFirst().contractName()).isEqualTo("Loan Agreement");
        assertThat(result.contracts().getFirst().contractUrl()).isEqualTo("https://example.com/contract.pdf");
        assertThat(result.contracts().getFirst().signStatus()).isNull();

        ArgumentCaptor<ContractFileRepository.ContractFileUpsert> upsertCaptor =
                ArgumentCaptor.forClass(ContractFileRepository.ContractFileUpsert.class);
        verify(contractFileRepository).upsert(upsertCaptor.capture());
        assertThat(upsertCaptor.getValue().loanApplicationId()).isEqualTo(10L);
        assertThat(upsertCaptor.getValue().billNo()).isEqualTo("BILL-1");
        assertThat(upsertCaptor.getValue().contractType()).isEqualTo("LOAN_AGREEMENT");
    }

    @Test
    void throwsWhenLoanApplicationNotFound() {
        when(loanApplicationRepository.findByLoanApplyIdAndProfileId("LOAN-404", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.listContracts(1L, "LOAN-404"))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
    }

    private static LoanApplicationRepository.LoanApplicationRecord loanApplication(long id) {
        return new LoanApplicationRepository.LoanApplicationRecord(
                id,
                "LOAN-1",
                100L,
                200L,
                1L,
                300L,
                null,
                null,
                "PROCESSING",
                null,
                new BigDecimal("1500000"),
                null,
                null
        );
    }
}
