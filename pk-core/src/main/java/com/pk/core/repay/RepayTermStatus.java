package com.pk.core.repay;

public final class RepayTermStatus {
    public static final String UNPAID = "UNPAID";
    public static final String OVERDUE = "OVERDUE";
    public static final String PAID = "PAID";

    private RepayTermStatus() {
    }

    public static String fromLenderStatus(String lenderStatus) {
        if (lenderStatus == null || lenderStatus.isBlank()) {
            return null;
        }
        return switch (lenderStatus) {
            case "SETTLE" -> PAID;
            case "OVERDUE" -> OVERDUE;
            case "NORMAL", "DUE" -> UNPAID;
            default -> null;
        };
    }

    public static boolean isPending(String termStatus) {
        return UNPAID.equals(termStatus) || OVERDUE.equals(termStatus);
    }
}
