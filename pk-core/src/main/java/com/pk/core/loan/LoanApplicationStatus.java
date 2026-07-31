package com.pk.core.loan;

public final class LoanApplicationStatus {
    public static final String INIT = "INIT";
    public static final String SUBMITTING = "SUBMITTING";
    public static final String PROCESSING = "PROCESSING";
    public static final String DISBURSED = "DISBURSED";
    public static final String REJECTED = "REJECTED";
    public static final String FAILED = "FAILED";
    public static final String CANCEL = "CANCEL";

    private LoanApplicationStatus() {
    }

    public static boolean isTerminal(String status) {
        return DISBURSED.equals(status)
                || REJECTED.equals(status)
                || FAILED.equals(status)
                || CANCEL.equals(status);
    }

    public static boolean isInFlight(String status) {
        return INIT.equals(status) || SUBMITTING.equals(status) || PROCESSING.equals(status);
    }
}
