package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AdvanceAiLenderRawOcrDetailSupportTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void wrapsDataOnlyPayloadAndAddsKtpIdNumberAlias() throws Exception {
        String dataOnly = """
                {"idNumber":"3603281301870006","name":"STEFANUS NURYANTO","placeOfIssue":"","issueDate":""}
                """;

        String prepared = AdvanceAiLenderRawOcrDetailSupport.prepareForLender(objectMapper, dataOnly);
        JsonNode root = objectMapper.readTree(prepared);

        assertThat(root.path("code").asText()).isEqualTo("SUCCESS");
        assertThat(root.path("data").path("ktpIdNumber").asText()).isEqualTo("3603281301870006");
        assertThat(root.path("data").has("placeOfIssue")).isFalse();
        assertThat(root.path("data").has("issueDate")).isFalse();
    }

    @Test
    void normalizesFullAdvanceAiEnvelope() throws Exception {
        String envelope = """
                {"code":"SUCCESS","message":"OK","data":{"idNumber":"3603281301870006","name":"STEFANUS NURYANTO","placeOfIssue":"","issueDate":""},"transactionId":"t1"}
                """;

        String prepared = AdvanceAiLenderRawOcrDetailSupport.prepareForLender(objectMapper, envelope);
        JsonNode root = objectMapper.readTree(prepared);

        assertThat(root.path("code").asText()).isEqualTo("SUCCESS");
        assertThat(root.path("data").path("ktpIdNumber").asText()).isEqualTo("3603281301870006");
        assertThat(root.path("transactionId").asText()).isEqualTo("t1");
    }

    @Test
    void extractsDataNodeFromEnvelope() throws Exception {
        String envelope = """
                {"code":"SUCCESS","data":{"idNumber":"3603281301870006","name":"Alice"}}
                """;

        JsonNode data = AdvanceAiLenderRawOcrDetailSupport.extractDataNode(objectMapper, envelope);

        assertThat(data.path("name").asText()).isEqualTo("Alice");
    }
}
