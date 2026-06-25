package com.pk.core.loan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RepaymentUniformityTest {
    @Test
    void treatsEvenRepayMethodAsUniform() {
        LenderRepayMethod method = new LenderRepayMethod(
                "RP001",
                "D",
                30,
                6,
                180,
                0,
                List.of()
        );

        assertTrue(RepaymentUniformity.isUniform(method));
    }

    @Test
    void treatsUnevenRepayMethodTypeAsNonUniform() {
        LenderRepayMethod method = new LenderRepayMethod(
                "RP002",
                "D",
                30,
                2,
                60,
                1,
                List.of(
                        new LenderRepayMethod.UnevenBillRate(1, new BigDecimal("0.6")),
                        new LenderRepayMethod.UnevenBillRate(2, new BigDecimal("0.4"))
                )
        );

        assertFalse(RepaymentUniformity.isUniform(method));
    }
}
