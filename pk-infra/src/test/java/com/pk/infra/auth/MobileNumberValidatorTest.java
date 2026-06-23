package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MobileNumberValidatorTest {
    @Test
    void acceptsValidIndonesiaMobileNumber() {
        assertThat(MobileNumberValidator.isValid("81234567890")).isTrue();
    }

    @Test
    void rejectsInvalidPrefixesAndFormats() {
        assertThat(MobileNumberValidator.isValid("+8613812345678")).isFalse();
        assertThat(MobileNumberValidator.isValid("081234567890")).isFalse();
        assertThat(MobileNumberValidator.isValid("62 812345678")).isFalse();
        assertThat(MobileNumberValidator.isValid("12345678")).isFalse();
    }
}
