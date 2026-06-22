package com.pk.core.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MobileNumberValidatorTest {
    @Test
    void acceptsValidIndonesianMobile() {
        assertTrue(MobileNumberValidator.isValid("81234567890"));
    }

    @Test
    void rejectsInvalidFormats() {
        assertFalse(MobileNumberValidator.isValid("81234567"));
        assertFalse(MobileNumberValidator.isValid("081234567890"));
        assertFalse(MobileNumberValidator.isValid("6281234567890"));
        assertFalse(MobileNumberValidator.isValid("+8613812345678"));
        assertFalse(MobileNumberValidator.isValid("8123 4567890"));
    }
}
