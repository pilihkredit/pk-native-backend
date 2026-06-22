package com.pk.core.credit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CreditStateMachineTest {
    @Test
    void allowsHappyPath() {
        assertTrue(CreditStateMachine.canTransition(CreditStatus.INIT, CreditStatus.SUBMITTING));
        assertTrue(CreditStateMachine.canTransition(CreditStatus.SUBMITTING, CreditStatus.PROCESSING));
        assertTrue(CreditStateMachine.canTransition(CreditStatus.PROCESSING, CreditStatus.APPROVED));
    }

    @Test
    void blocksTerminalRollback() {
        assertFalse(CreditStateMachine.canTransition(CreditStatus.APPROVED, CreditStatus.PROCESSING));
        assertFalse(CreditStateMachine.canTransition(CreditStatus.REJECTED, CreditStatus.INIT));
    }
}
