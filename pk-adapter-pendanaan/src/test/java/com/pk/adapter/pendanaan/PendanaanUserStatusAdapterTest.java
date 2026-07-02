package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendanaanUserStatusAdapterTest {
    @Mock
    private PendanaanHttpClient httpClient;

    @Test
    void mapsLenderUserStatusResponse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        PendanaanUserStatusAdapter adapter = new PendanaanUserStatusAdapter(httpClient, objectMapper);
        when(httpClient.post(
                eq(PendanaanUserStatusAdapter.USER_STATUS_PATH),
                org.mockito.ArgumentMatchers.anyString(),
                eq(PendanaanUserStatusAdapter.BUSINESS_TYPE),
                eq("U10001")
        )).thenReturn(objectMapper.readTree("""
                {
                  "partnerUserId": "U10001",
                  "userId": "USR-1",
                  "userLoanLifeTimeStatus": 4,
                  "userLoanLifeTimeLastAction": 22,
                  "freezeEndTime": null,
                  "onLoanCount": 0,
                  "creditContractExpireTime": 1780300800000
                }
                """));

        LenderUserStatusPort.LenderUserStatusResult result = adapter.queryStatus(
                new LenderUserStatusPort.LenderUserStatusCommand(
                        "U10001",
                        new LenderDeviceContext(
                                "LenderApp",
                                "1.0.0",
                                "com.example",
                                "device-1",
                                "android",
                                null,
                                null,
                                "ClientApp"
                        )
                )
        );

        assertThat(result.partnerUserId()).isEqualTo("U10001");
        assertThat(result.lenderUserId()).isEqualTo("USR-1");
        assertThat(result.userLoanLifeTimeStatus()).isEqualTo(4);
        assertThat(result.userLoanLifeTimeLastAction()).isEqualTo(22);
        assertThat(result.onLoanCount()).isZero();
        assertThat(result.creditContractExpireTime()).isEqualTo(1780300800000L);
        assertThat(result.requestJson()).contains("partnerUserId");
        assertThat(result.responseDataJson()).contains("USR-1");
    }
}
