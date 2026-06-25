package com.pk.core.repay;

public final class RepayBillStatus {
    public static final String NORMAL = "NORMAL";
    public static final String OVERDUE = "OVERDUE";
    public static final String SETTLE = "SETTLE";

    private RepayBillStatus() {
    }

    public static String fromLenderStatus(String lenderStatus) {
        if (lenderStatus == null || lenderStatus.isBlank()) {
            return null;
        }
        return switch (lenderStatus) {
            case "SETTLE" -> SETTLE;
            case "OVERDUE" -> OVERDUE;
            case "NORMAL" -> NORMAL;
            default -> null;
        };
    }
}
