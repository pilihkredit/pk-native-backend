package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.LenderProfileQueryPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileQueryFacadeTest {
    private LenderProfileQueryPort lenderProfileQueryPort;
    private ProfileQueryFacade facade;

    @BeforeEach
    void setUp() {
        lenderProfileQueryPort = mock(LenderProfileQueryPort.class);
        facade = new ProfileQueryFacade(lenderProfileQueryPort, new ObjectMapper());
    }

    @Test
    void returnsLegacyBankCardObjectFromBankCardList() {
        when(lenderProfileQueryPort.query(any())).thenReturn(
                new LenderProfileQueryPort.LenderProfileQueryResult("""
                        {
                          "partnerUserId": "U10001",
                          "bankCardList": [
                            {
                              "bankCardId": 10001,
                              "bankCode": "BCA",
                              "bankName": "Bank Central Asia",
                              "cardNumber": "1234567890",
                              "isDefault": true
                            }
                          ]
                        }
                        """)
        );

        JsonNode result = facade.query("U10001", List.of("bankCard"));

        assertThat(result.path("bankCard").path("bankCode").asText()).isEqualTo("BCA");
        assertThat(result.path("bankCard").path("cardNumber").asText()).isEqualTo("1234567890");
        assertThat(result.path("bankCardList").isArray()).isTrue();
        assertThat(result.path("bankCardList")).hasSize(1);
    }
}
