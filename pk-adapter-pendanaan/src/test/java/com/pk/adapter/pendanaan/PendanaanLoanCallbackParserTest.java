package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.LoanCallbackParser;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PendanaanLoanCallbackParserTest {
    private PendanaanLoanCallbackParser parser;

    @BeforeEach
    void setUp() {
        parser = new PendanaanLoanCallbackParser(new ObjectMapper());
    }

    @Test
    void parsesDisbursedCallback() {
        String json = """
                {
                  "loanApplyId": "LOAN-001",
                  "loanApplyNo": "LN-001",
                  "applyStatus": "SUCCESS",
                  "billNo": "BN-001",
                  "applyAmt": 1500000,
                  "payAmount": 1455000,
                  "payTime": 1749792000000,
                  "freezeEndTime": null
                }
                """;

        LoanCallbackParser.ParsedLoanCallback parsed = parser.parse(json);

        assertThat(parsed.loanApplyId()).isEqualTo("LOAN-001");
        assertThat(parsed.loanApplyNo()).isEqualTo("LN-001");
        assertThat(parsed.externalStatus()).isEqualTo("SUCCESS");
        assertThat(parsed.billNo()).isEqualTo("BN-001");
        assertThat(parsed.applyAmt()).isEqualByComparingTo(new BigDecimal("1500000"));
        assertThat(parsed.payAmount()).isEqualByComparingTo(new BigDecimal("1455000"));
        assertThat(parsed.payTime()).isEqualTo(1749792000000L);
        assertThat(parsed.freezeEndTime()).isNull();
    }

    @Test
    void parsesRefusedCallbackWithFreezeEndTime() {
        String json = """
                {
                  "loanApplyId": "LOAN-002",
                  "loanApplyNo": "LN-002",
                  "applyStatus": "REFUSED",
                  "billNo": null,
                  "applyAmt": 800000,
                  "payAmount": null,
                  "payTime": null,
                  "freezeEndTime": 1749200000000
                }
                """;

        LoanCallbackParser.ParsedLoanCallback parsed = parser.parse(json);

        assertThat(parsed.externalStatus()).isEqualTo("REFUSED");
        assertThat(parsed.freezeEndTime()).isEqualTo(1749200000000L);
    }

    @Test
    void rejectsMissingLoanApplyId() {
        assertThatThrownBy(() -> parser.parse("{\"applyStatus\":\"SUCCESS\"}"))
                .isInstanceOf(ApiException.class);
    }
}
