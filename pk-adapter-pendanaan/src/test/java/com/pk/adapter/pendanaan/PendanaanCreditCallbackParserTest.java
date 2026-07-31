package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.CreditCallbackParser;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PendanaanCreditCallbackParserTest {
    private PendanaanCreditCallbackParser parser;

    @BeforeEach
    void setUp() {
        parser = new PendanaanCreditCallbackParser(new ObjectMapper());
    }

    @Test
    void parsesSuccessCallback() {
        String json = """
                {
                  "applyId": "AP-001",
                  "creditApplyNo": "CA-001",
                  "status": "SUCCESS",
                  "creditContractExpireTime": 1893456000000,
                  "riskMinLimit": 1000000,
                  "riskMaxLimit": 5000000,
                  "psychologicalCreditLimit": 4500000,
                  "fakeCreditLimit": 5000000,
                  "borrowAmtStepSize": 100000
                }
                """;

        CreditCallbackParser.ParsedCreditCallback parsed = parser.parse(json);

        assertThat(parsed.applyId()).isEqualTo("AP-001");
        assertThat(parsed.creditApplyNo()).isEqualTo("CA-001");
        assertThat(parsed.externalStatus()).isEqualTo("SUCCESS");
        assertThat(parsed.creditContractExpireTime()).isEqualTo(1893456000000L);
        assertThat(parsed.riskMinLimit()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(parsed.borrowAmtStepSize()).isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    void parsesRefusedCallbackWithFreezeEndTime() {
        String json = """
                {
                  "applyId": "AP-002",
                  "creditApplyNo": "CA-002",
                  "status": "REFUSED",
                  "freezeEndTime": 1780300800000
                }
                """;

        CreditCallbackParser.ParsedCreditCallback parsed = parser.parse(json);

        assertThat(parsed.externalStatus()).isEqualTo("REFUSED");
        assertThat(parsed.freezeEndTime()).isEqualTo(1780300800000L);
        assertThat(parsed.creditContractExpireTime()).isNull();
    }

    @Test
    void rejectsMissingApplyId() {
        assertThatThrownBy(() -> parser.parse("{\"status\":\"SUCCESS\"}"))
                .isInstanceOf(ApiException.class);
    }
}
