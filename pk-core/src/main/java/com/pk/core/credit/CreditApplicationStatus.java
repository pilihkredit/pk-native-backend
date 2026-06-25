package com.pk.core.credit;

public final class CreditApplicationStatus {
    public static final String INIT = "INIT";
    public static final String SUBMITTING = "SUBMITTING";
    public static final String PROCESSING = "PROCESSING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String FAILED = "FAILED";
    public static final String CANCEL = "CANCEL";

    private CreditApplicationStatus() {
    }

    public static boolean isTerminal(String status) {
        return APPROVED.equals(status)
                || REJECTED.equals(status)
                || FAILED.equals(status)
                || CANCEL.equals(status);
    }

    public static boolean isInFlight(String status) {
        return INIT.equals(status) || SUBMITTING.equals(status) || PROCESSING.equals(status);
    }
}
