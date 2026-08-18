package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {
    @Test
    void createsFailureResponseWithCustomMessage() {
        ApiResponse<Object> response = ApiResponse.failure(
                ApiCode.INVALID_REQUEST_PARAMETERS,
                "requestId: must not be blank",
                "trace-789"
        );

        assertThat(response.code()).isEqualTo("K000001");
        assertThat(response.msg()).isEqualTo("requestId: must not be blank");
        assertThat(response.data()).isNull();
        assertThat(response.traceId()).isEqualTo("trace-789");
        assertThat(response.lenderProvider()).isNull();
        assertThat(response.lenderProviderName()).isNull();
    }

    @Test
    void createsFailureResponseFromApiCode() {
        ApiResponse<Object> response = ApiResponse.failure(ApiCode.INVALID_REQUEST_PARAMETERS, "trace-789");

        assertThat(response.code()).isEqualTo("K000001");
        assertThat(response.msg()).isEqualTo("Invalid request parameters");
        assertThat(response.data()).isNull();
        assertThat(response.traceId()).isEqualTo("trace-789");
    }

    @Test
    void rejectsSuccessCodeForFailureResponse() {
        assertThatThrownBy(() -> ApiResponse.failure(ApiCode.SUCCESS, "trace-789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Success code cannot be used for failure responses");
    }

    @Test
    void withLenderProviderCopiesEnvelopeFields() {
        ApiResponse<String> enriched = ApiResponse.success("ok", "trace-1")
                .withLenderProvider("apipartner", "ApiPartner Test");

        assertThat(enriched.code()).isEqualTo(ApiCode.SUCCESS.code());
        assertThat(enriched.data()).isEqualTo("ok");
        assertThat(enriched.traceId()).isEqualTo("trace-1");
        assertThat(enriched.lenderProvider()).isEqualTo("apipartner");
        assertThat(enriched.lenderProviderName()).isEqualTo("ApiPartner Test");
    }
}
