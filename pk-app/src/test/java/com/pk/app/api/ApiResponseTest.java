package com.pk.app.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {
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
}
