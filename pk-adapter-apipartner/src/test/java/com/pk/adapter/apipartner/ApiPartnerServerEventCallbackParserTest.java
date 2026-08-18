package com.pk.adapter.apipartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.ServerEventCallbackParser;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiPartnerServerEventCallbackParserTest {
    private ApiPartnerServerEventCallbackParser parser;

    @BeforeEach
    void setUp() {
        parser = new ApiPartnerServerEventCallbackParser(new ObjectMapper());
    }

    @Test
    void parsesServerEventPushPayload() {
        String json = """
                {
                  "eventId": "USR202506020001",
                  "eventType": "BASIC_AUTH_FINISH",
                  "eventTime": 1749792000000,
                  "eventValue": null,
                  "value": 10.5,
                  "clientId": "open_client_demo",
                  "userId": "USR202506020001",
                  "partnerUserId": "OPEN_USER_10001",
                  "appName": "cashloan",
                  "countryCode": "ID",
                  "appVersion": "4.8.0",
                  "countryName": "Indonesia",
                  "deviceNo": "DEVICE202506020001",
                  "systemPlatform": "Android",
                  "adId": "gaid-demo-value"
                }
                """;

        ServerEventCallbackParser.ParsedServerEventCallback parsed = parser.parse(json);

        assertThat(parsed.eventId()).isEqualTo("USR202506020001");
        assertThat(parsed.eventType()).isEqualTo("BASIC_AUTH_FINISH");
        assertThat(parsed.eventTime()).isEqualTo(1749792000000L);
        assertThat(parsed.value()).isEqualByComparingTo(new BigDecimal("10.5"));
        assertThat(parsed.partnerUserId()).isEqualTo("OPEN_USER_10001");
    }

    @Test
    void rejectsMissingEventId() {
        assertThatThrownBy(() -> parser.parse("{\"eventType\":\"BASIC_AUTH_FINISH\",\"eventTime\":1749792000000}"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsMissingEventType() {
        assertThatThrownBy(() -> parser.parse("{\"eventId\":\"EVT-001\",\"eventTime\":1749792000000}"))
                .isInstanceOf(ApiException.class);
    }
}
