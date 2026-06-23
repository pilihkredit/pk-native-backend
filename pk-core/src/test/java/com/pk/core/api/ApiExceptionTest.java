package com.pk.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApiExceptionTest {
    @Test
    void keepsApiCodeAsCanonicalMessage() {
        ApiException exception = new ApiException(ApiCode.INVALID_LOAN_AMOUNT);

        assertEquals(ApiCode.INVALID_LOAN_AMOUNT, exception.apiCode());
        assertEquals("Invalid loan amount", exception.getMessage());
    }

    @Test
    void rejectsSuccessCode() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new ApiException(ApiCode.SUCCESS)
        );

        assertTrue(error.getMessage().contains("Success code cannot be used for exceptions"));
    }
}
