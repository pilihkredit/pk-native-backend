package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.ProfileSyncModule;
import org.junit.jupiter.api.Test;

class PendanaanProfileCodeMapperTest {
    @Test
    void mapsVaCardToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000104",
                ProfileSyncModule.BANK_CARD
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_VA_NOT_ALLOWED);
    }

    @Test
    void mapsVerificationFailureToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000321",
                ProfileSyncModule.BANK_CARD
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_VERIFICATION_FAILED);
    }

    @Test
    void mapsAlreadyBoundToPublicCode() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000339",
                ProfileSyncModule.BANK_CARD
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.BANK_CARD_ALREADY_BOUND);
    }

    @Test
    void mapsLenderParameterErrorToL000001() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000001",
                "请求参数错误",
                ProfileSyncModule.PERSONAL
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_REQUEST_PARAMETERS);
        assertThat(exception.detail()).isEqualTo("请求参数错误");
    }

    @Test
    void mapsIdentityFaceFailureToL000052() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000052",
                ProfileSyncModule.IDENTITY
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_FACE_RECOGNITION_FAILED);
    }

    @Test
    void mapsIdentityEktpFailureToL000053() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000053",
                ProfileSyncModule.IDENTITY
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_EKTP_FORMAT);
    }

    @Test
    void mapsIdentityOcrRawDetailFailureToL000445() {
        ApiException exception = PendanaanProfileCodeMapper.toApiException(
                "A000445",
                ProfileSyncModule.IDENTITY
        );
        assertThat(exception.apiCode()).isEqualTo(ApiCode.LENDER_INVALID_OCR_RAW_DETAIL);
    }
}
