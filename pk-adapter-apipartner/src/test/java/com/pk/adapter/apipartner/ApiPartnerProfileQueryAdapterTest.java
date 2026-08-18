package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.LenderProfileQueryPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiPartnerProfileQueryAdapterTest {
    @Mock
    private ApiPartnerHttpClient httpClient;

    @Test
    void mapsLenderProfileQueryResponse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiPartnerProfileQueryAdapter adapter = new ApiPartnerProfileQueryAdapter(httpClient, objectMapper);
        when(httpClient.post(
                eq(ApiPartnerProfileQueryAdapter.USER_INFO_QUERY_PATH),
                org.mockito.ArgumentMatchers.anyString(),
                eq(ApiPartnerProfileQueryAdapter.BUSINESS_TYPE),
                eq("U10001")
        )).thenReturn(objectMapper.readTree("""
                {
                  "partnerUserId": "U10001",
                  "userId": "USR-1",
                  "mobileNo": {
                    "mobileNo": "081234567890"
                  },
                  "identity": {
                    "name": "OPEN USER",
                    "idNo": "3201010101010001"
                  }
                }
                """));

        LenderProfileQueryPort.LenderProfileQueryResult result = adapter.query(
                new LenderProfileQueryPort.LenderProfileQueryCommand(
                        "U10001",
                        List.of("mobileNo", "identity")
                )
        );

        assertThat(result.rawResponseJson()).contains("U10001");
        assertThat(result.rawResponseJson()).contains("USR-1");
        assertThat(result.rawResponseJson()).contains("081234567890");
        assertThat(result.rawResponseJson()).contains("OPEN USER");
    }
}
