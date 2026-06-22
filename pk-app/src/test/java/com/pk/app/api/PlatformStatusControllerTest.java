package com.pk.app.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class PlatformStatusControllerTest {
    @Test
    void returnsWrappedPlatformStatus() {
        var request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-123");

        ApiResponse<PlatformStatus> response = new PlatformStatusController().status(request);

        assertThat(response.code()).isEqualTo("000000");
        assertThat(response.msg()).isEqualTo("success");
        assertThat(response.traceId()).isEqualTo("trace-123");
        assertThat(response.data().status()).isEqualTo("UP");
        assertThat(response.data().time()).isNotNull();
    }
}
