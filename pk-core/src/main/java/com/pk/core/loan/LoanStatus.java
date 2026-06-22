package com.pk.core.loan;

public enum LoanStatus {
    INIT,
    SUBMITTING,
    PROCESSING,
    DISBURSED,
    REJECTED,
    FAILED;

    public boolean isTerminal() {
        return this == DISBURSED || this == REJECTED || this == FAILED;
    }
}
