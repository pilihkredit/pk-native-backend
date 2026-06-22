package com.pk.core.loan;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class LoanStateMachine {
    private static final Map<LoanStatus, Set<LoanStatus>> TRANSITIONS = Map.of(
            LoanStatus.INIT, EnumSet.of(LoanStatus.SUBMITTING),
            LoanStatus.SUBMITTING, EnumSet.of(LoanStatus.PROCESSING, LoanStatus.FAILED),
            LoanStatus.PROCESSING, EnumSet.of(LoanStatus.DISBURSED, LoanStatus.FAILED, LoanStatus.REJECTED),
            LoanStatus.DISBURSED, EnumSet.noneOf(LoanStatus.class),
            LoanStatus.REJECTED, EnumSet.noneOf(LoanStatus.class),
            LoanStatus.FAILED, EnumSet.noneOf(LoanStatus.class)
    );

    private LoanStateMachine() {
    }

    public static boolean canTransition(LoanStatus current, LoanStatus next) {
        if (current == null || next == null) {
            return false;
        }
        if (current.isTerminal()) {
            return false;
        }
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(next);
    }
}
