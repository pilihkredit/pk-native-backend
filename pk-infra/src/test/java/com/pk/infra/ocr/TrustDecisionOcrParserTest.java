package com.pk.infra.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TrustDecisionOcrParserTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsIndonesiaKtpFields() throws Exception {
        var root = objectMapper.readTree("""
                {
                  "code": 200,
                  "result": "success",
                  "sequence_id": "ocr-seq",
                  "card_info": {
                    "nik": "3201010101010001",
                    "name": "TEST USER",
                    "birthday": "01-01-2000",
                    "birthplace": "JAKARTA",
                    "gender": "LAKI-LAKI",
                    "religion": "ISLAM",
                    "marital_status": "BELUM KAWIN",
                    "occupation": "EMPLOYEE",
                    "nationality": "WNI",
                    "address": "TEST ADDRESS",
                    "province": "DKI JAKARTA",
                    "city": "JAKARTA",
                    "street": "TEST DISTRICT"
                  }
                }
                """);

        var parsed = TrustDecisionOcrParser.parse(root);

        assertThat(parsed.ocrName()).isEqualTo("TEST USER");
        assertThat(parsed.ocrIdNo()).isEqualTo("3201010101010001");
        assertThat(parsed.birthday()).isEqualTo("01-01-2000");
        assertThat(parsed.district()).isEqualTo("TEST DISTRICT");
    }

    @Test
    void returnsNullWhenOcrBusinessResultFails() throws Exception {
        var root = objectMapper.readTree("""
                {"code":200,"result":"fail","sequence_id":"ocr-seq"}
                """);

        assertThat(TrustDecisionOcrParser.parse(root)).isNull();
    }
}
