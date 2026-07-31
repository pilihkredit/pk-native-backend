package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ocr.OcrSessionState;
import org.junit.jupiter.api.Test;

class AdvanceAiRawOcrDetailBuilderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void buildsAdvanceAiRawFieldsNotPkNormalizedNames() throws Exception {
        OcrSessionState.OcrParsedFields parsed = new OcrSessionState.OcrParsedFields(
                "STEFANUS NURYANTO",
                "3603281301870006",
                "LAKI-LAKI",
                "KATHOLIK",
                "KAWIN",
                "1987-01-13",
                "JAKARTA",
                "BANJARSARI",
                "KARYAWAN SWASTA",
                "WNI",
                "O",
                "SEUMUR HIDUP",
                "DAERAH ISTIMEWA YOGYAKARTA",
                "KABUPATEN SLEMAN",
                "PAKEM"
        );

        String rawOcrDetail = AdvanceAiRawOcrDetailBuilder.build(objectMapper, parsed);
        JsonNode node = objectMapper.readTree(rawOcrDetail);

        assertThat(node.path("name").asText()).isEqualTo("STEFANUS NURYANTO");
        assertThat(node.path("ktpIdNumber").asText()).isEqualTo("3603281301870006");
        assertThat(node.path("gender").asText()).isEqualTo("LAKI-LAKI");
        assertThat(node.path("birthPlaceBirthday").asText()).isEqualTo("JAKARTA, 13-01-1987");
        assertThat(node.has("dateOfBirth")).isTrue();
        assertThat(node.has("ocrName")).isFalse();
        assertThat(node.has("ocrIdNo")).isFalse();
        assertThat(node.has("ocrChannel")).isFalse();
    }
}
