package com.pk.infra.credit;

import com.pk.core.credit.CreditApplicationStatus;

public final class CreditExternalStatusMapper {
    private CreditExternalStatusMapper() {
    }

    public static String toPublicStatus(String internalStatus) {
        if (internalStatus == null) {
            return CreditApplicationStatus.PROCESSING;
        }
        return switch (internalStatus) {
            case CreditApplicationStatus.APPROVED -> CreditApplicationStatus.APPROVED;
            case CreditApplicationStatus.REJECTED -> CreditApplicationStatus.REJECTED;
            case CreditApplicationStatus.FAILED -> CreditApplicationStatus.FAILED;
            case CreditApplicationStatus.CANCEL -> CreditApplicationStatus.CANCEL;
            default -> CreditApplicationStatus.PROCESSING;
        };
    }

    public static String mapLenderStatus(String lenderStatus) {
        if (lenderStatus == null || lenderStatus.isBlank()) {
            return CreditApplicationStatus.PROCESSING;
        }
        return switch (lenderStatus.trim().toUpperCase()) {
            case "SUCCESS" -> CreditApplicationStatus.APPROVED;
            case "REFUSED" -> CreditApplicationStatus.REJECTED;
            case "FAIL" -> CreditApplicationStatus.FAILED;
            case "CANCEL" -> CreditApplicationStatus.CANCEL;
            case "PROCESSING" -> CreditApplicationStatus.PROCESSING;
            default -> CreditApplicationStatus.PROCESSING;
        };
    }
}
