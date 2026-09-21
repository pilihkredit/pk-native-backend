package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;

class ApiPartnerUserDisableAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void treatsAlreadyDisabledAsSuccess() {
        ApiPartnerHttpClient httpClient = mock(ApiPartnerHttpClient.class);
        when(httpClient.postEnvelopeWithInteraction(
                eq(ApiPartnerUserDisableAdapter.DISABLE_PATH),
                any(),
                eq(ApiPartnerUserDisableAdapter.DISABLE_BUSINESS_TYPE),
                eq("U10")
        )).thenReturn(new ApiPartnerHttpClient.EnvelopeResult(
                objectMapper.createObjectNode()
                        .put("code", "A000036")
                        .put("msg", "already disabled"),
                1L
        ));

        assertThatCode(() -> new ApiPartnerUserDisableAdapter(httpClient, objectMapper).disableUser("U10"))
                .doesNotThrowAnyException();
    }

    @Test
    void mapsBusinessBlockToAccountCloseNotAllowed() {
        ApiPartnerHttpClient httpClient = mock(ApiPartnerHttpClient.class);
        when(httpClient.postEnvelopeWithInteraction(any(), any(), any(), any()))
                .thenReturn(new ApiPartnerHttpClient.EnvelopeResult(
                        objectMapper.createObjectNode()
                                .put("code", "A000332")
                                .put("msg", "unpaid bill"),
                        2L
                ));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> new ApiPartnerUserDisableAdapter(httpClient, objectMapper).disableUser("U10")
        )
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.ACCOUNT_CLOSE_NOT_ALLOWED);
    }
}
