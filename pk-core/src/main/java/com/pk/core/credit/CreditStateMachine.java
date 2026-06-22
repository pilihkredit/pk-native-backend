package com.pk.core.credit;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class CreditStateMachine {
    private static final Map<CreditStatus, Set<CreditStatus>> TRANSITIONS = Map.of(
            CreditStatus.INIT, EnumSet.of(CreditStatus.SUBMITTING),
            CreditStatus.SUBMITTING, EnumSet.of(CreditStatus.PROCESSING, CreditStatus.FAILED),
            CreditStatus.PROCESSING, EnumSet.of(CreditStatus.APPROVED, CreditStatus.REJECTED, CreditStatus.FAILED),
            CreditStatus.APPROVED, EnumSet.noneOf(CreditStatus.class),
            CreditStatus.REJECTED, EnumSet.noneOf(CreditStatus.class),
            CreditStatus.FAILED, EnumSet.noneOf(CreditStatus.class)
    );

    private CreditStateMachine() {
    }

    public static boolean canTransition(CreditStatus current, CreditStatus next) {
        if (current == null || next == null) {
            return false;
        }
        if (current.isTerminal()) {
            return false;
        }
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(next);
    }
}
