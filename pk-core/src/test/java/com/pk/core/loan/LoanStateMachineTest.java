package com.pk.core.loan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoanStateMachineTest {
    @Test
    void allowsHappyPath() {
        assertTrue(LoanStateMachine.canTransition(LoanStatus.INIT, LoanStatus.SUBMITTING));
        assertTrue(LoanStateMachine.canTransition(LoanStatus.SUBMITTING, LoanStatus.PROCESSING));
        assertTrue(LoanStateMachine.canTransition(LoanStatus.PROCESSING, LoanStatus.DISBURSED));
    }

    @Test
    void blocksTerminalRollback() {
        assertFalse(LoanStateMachine.canTransition(LoanStatus.DISBURSED, LoanStatus.PROCESSING));
        assertFalse(LoanStateMachine.canTransition(LoanStatus.FAILED, LoanStatus.INIT));
    }
}
