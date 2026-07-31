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
    void keepsDetailForLoggingWithoutChangingApiCodeMessage() {
        ApiException exception = new ApiException(
                ApiCode.INVALID_REQUEST_PARAMETERS,
                "device.deviceNo does not match header X-Device-No"
        );

        assertEquals(ApiCode.INVALID_REQUEST_PARAMETERS, exception.apiCode());
        assertEquals(
                "device.deviceNo does not match header X-Device-No",
                exception.detail()
        );
        assertEquals(
                "Invalid request parameters: device.deviceNo does not match header X-Device-No",
                exception.getMessage()
        );
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
