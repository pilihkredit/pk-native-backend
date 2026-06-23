package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;

class MotherSurnameValidatorTest {
    @Test
    void acceptsValidMotherSurname() {
        assertThatCode(() -> MotherSurnameValidator.validate("Siti")).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankMotherSurname() {
        assertThatThrownBy(() -> MotherSurnameValidator.validate(" "))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.MOTHER_SURNAME_REQUIRED);
    }

    @Test
    void rejectsNumericMotherSurname() {
        assertThatThrownBy(() -> MotherSurnameValidator.validate("12345"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_MOTHER_SURNAME_FORMAT);
    }
}
