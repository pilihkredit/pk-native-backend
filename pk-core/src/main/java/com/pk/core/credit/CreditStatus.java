package com.pk.core.credit;

public enum CreditStatus {
    INIT,
    SUBMITTING,
    PROCESSING,
    APPROVED,
    REJECTED,
    FAILED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == FAILED;
    }
}
