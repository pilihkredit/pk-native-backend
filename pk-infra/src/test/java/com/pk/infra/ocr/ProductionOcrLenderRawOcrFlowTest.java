package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Covers the production OCR path: §17 stores Advance.ai envelope, §19 lender sync reads normalized raw JSON.
 */
class ProductionOcrLenderRawOcrFlowTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void normalizesAdvanceAiEnvelopeLikeProductionOcrCheck() throws Exception {
        String sessionOcrRawJson = """
                {"code":"SUCCESS","message":"OK","data":{"idNumber":"3603281301870006","name":"STEFANUS NURYANTO","bloodType":"O","religion":"KATHOLIK","gender":"LAKI-LAKI","birthPlaceBirthday":"JAKARTA, 13-01-1987","province":"DAERAH ISTIMEWA YOGYAKARTA","city":"KABUPATEN SLEMAN","district":"PAKEM","village":"PAKEMBINANGUN","rtrw":"001/000","occupation":"KARYAWAN SWASTA","expiryDate":"SEUMUR HIDUP","nationality":"WNI","maritalStatus":"KAWIN","address":"BANJARSARI","placeOfIssue":"","placeOfBirth":"JAKARTA","birthday":"1987/01/13","issueDate":""},"transactionId":"981e2ae5a8d48e94","pricingStrategy":"PAY"}
                """;

        String lenderRawOcrDetail = AdvanceAiLenderRawOcrDetailSupport.prepareForLender(objectMapper, sessionOcrRawJson);
        JsonNode root = objectMapper.readTree(lenderRawOcrDetail);

        assertThat(root.path("code").asText()).isEqualTo("SUCCESS");
        assertThat(root.path("data").path("ktpIdNumber").asText()).isEqualTo("3603281301870006");
        assertThat(root.path("data").has("placeOfIssue")).isFalse();
        assertThat(root.path("data").has("issueDate")).isFalse();
        assertThat(AdvanceAiLenderRawOcrDetailSupport.extractDataNode(objectMapper, sessionOcrRawJson)
                .path("name")
                .asText()).isEqualTo("STEFANUS NURYANTO");
    }
}
