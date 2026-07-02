package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;

class ValidationFailureMessagesTest {
    @Test
    void usesApiExceptionDetailAsClientMessage() {
        ApiException exception = new ApiException(
                ApiCode.INVALID_REQUEST_PARAMETERS,
                "riskDataInfo.openUserDevice: must not be null"
        );

        assertThat(ValidationFailureMessages.forApiException(exception))
                .isEqualTo("riskDataInfo.openUserDevice: must not be null");
    }

    @Test
    void fallsBackToApiCodeMessageWhenDetailMissing() {
        ApiException exception = new ApiException(ApiCode.INVALID_LOAN_AMOUNT);

        assertThat(ValidationFailureMessages.forApiException(exception))
                .isEqualTo("Invalid loan amount");
    }

    @Test
    void ignoresLenderDetailForUpstreamBusinessCodes() {
        ApiException exception = new ApiException(
                ApiCode.LENDER_INVALID_OCR_RAW_DETAIL,
                "A000445: advanceAi OCR原始报文格式错误"
        );

        assertThat(ValidationFailureMessages.forApiException(exception))
                .isEqualTo(ApiCode.LENDER_INVALID_OCR_RAW_DETAIL.message());
    }
}
