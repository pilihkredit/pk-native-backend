package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;

class PendanaanProfileCodeMapperTest {
    @Test
    void mapsVaCardToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException("A000104", true);
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_VA_NOT_ALLOWED);
    }

    @Test
    void mapsVerificationFailureToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException("A000321", true);
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_VERIFICATION_FAILED);
    }

    @Test
    void mapsAlreadyBoundToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException("A000339", true);
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_ALREADY_BOUND);
    }
}
