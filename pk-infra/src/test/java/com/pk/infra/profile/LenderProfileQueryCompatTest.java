package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LenderProfileQueryCompatTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsDefaultBankCardListItemToLegacyBankCardObject() throws Exception {
        JsonNode root = objectMapper.readTree("""
                {
                  "partnerUserId": "U1",
                  "bankCardList": [
                    {
                      "bankCardId": 1,
                      "bankCode": "BRI",
                      "bankName": "Bank BRI",
                      "cardNumber": "1111",
                      "isDefault": false
                    },
                    {
                      "bankCardId": 2,
                      "bankCode": "BCA",
                      "bankName": "Bank Central Asia",
                      "cardNumber": "1234567890",
                      "isDefault": true
                    }
                  ]
                }
                """);

        JsonNode mapped = LenderProfileQueryCompat.applyBankCardCompat(root, objectMapper);

        assertThat(mapped.path("bankCard").path("bankCode").asText()).isEqualTo("BCA");
        assertThat(mapped.path("bankCard").path("cardNumber").asText()).isEqualTo("1234567890");
        assertThat(mapped.path("bankCard").path("bankName").asText()).isEqualTo("Bank Central Asia");
        assertThat(mapped.path("bankCard").path("cardName").asText()).isEqualTo("Bank Central Asia");
        assertThat(mapped.path("bankCardList").isArray()).isTrue();
        assertThat(mapped.path("bankCardList")).hasSize(2);
    }

    @Test
    void usesFirstItemWhenNoDefaultFlag() throws Exception {
        JsonNode root = objectMapper.readTree("""
                {
                  "bankCardList": [
                    { "bankCode": "BNI", "cardNumber": "9999", "bankName": "BNI" }
                  ]
                }
                """);

        JsonNode mapped = LenderProfileQueryCompat.applyBankCardCompat(root, objectMapper);

        assertThat(mapped.path("bankCard").path("bankCode").asText()).isEqualTo("BNI");
    }

    @Test
    void leavesPayloadUntouchedWhenBankCardListMissingOrEmpty() throws Exception {
        JsonNode emptyList = objectMapper.readTree("""
                { "bankCardList": [], "profile": { "educationDegree": 1 } }
                """);
        JsonNode mappedEmpty = LenderProfileQueryCompat.applyBankCardCompat(emptyList, objectMapper);
        assertThat(mappedEmpty.has("bankCard")).isFalse();

        JsonNode legacy = objectMapper.readTree("""
                { "bankCard": { "bankCode": "BCA", "cardNumber": "1" } }
                """);
        JsonNode mappedLegacy = LenderProfileQueryCompat.applyBankCardCompat(legacy, objectMapper);
        assertThat(mappedLegacy.path("bankCard").path("bankCode").asText()).isEqualTo("BCA");
    }
}
