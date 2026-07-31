package com.pk.infra.loan;

import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LoanApplicationRepository;

public final class LoanExternalStatusMapper {
    private LoanExternalStatusMapper() {
    }

    public static String toPublicStatus(String internalStatus) {
        if (internalStatus == null) {
            return LoanApplicationStatus.PROCESSING;
        }
        return switch (internalStatus) {
            case LoanApplicationStatus.DISBURSED -> LoanApplicationStatus.DISBURSED;
            case LoanApplicationStatus.REJECTED -> LoanApplicationStatus.REJECTED;
            case LoanApplicationStatus.FAILED -> LoanApplicationStatus.FAILED;
            case LoanApplicationStatus.CANCEL -> LoanApplicationStatus.CANCEL;
            default -> LoanApplicationStatus.PROCESSING;
        };
    }

    public static String mapLenderStatus(String lenderStatus) {
        if (lenderStatus == null || lenderStatus.isBlank()) {
            return LoanApplicationStatus.PROCESSING;
        }
        return switch (lenderStatus.trim().toUpperCase()) {
            case "SUCCESS" -> LoanApplicationStatus.DISBURSED;
            case "REFUSED" -> LoanApplicationStatus.REJECTED;
            case "FAIL" -> LoanApplicationStatus.FAILED;
            case "CANCEL" -> LoanApplicationStatus.CANCEL;
            case "PAYING", "PROCESSING" -> LoanApplicationStatus.PROCESSING;
            default -> LoanApplicationStatus.PROCESSING;
        };
    }

    public static String publicStatusOf(LoanApplicationRepository.LoanApplicationRecord record) {
        return toPublicStatus(record.status());
    }
}
