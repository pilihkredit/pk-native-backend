package com.pk.app.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiLogPayloadSupportTest {
    @Test
    void keepsPkEnvelopeMsgButRedactsNestedLenderMsg() {
        String payload = """
                {"code":"000000","msg":"success","data":{"requestId":"r1","lenderResponse":{"syncFailed":true,"code":"L000445","msg":"A000445: advanceAi OCR原始报文格式错误"}},"traceId":"t1"}
                """;

        String redacted = ApiLogPayloadSupport.redactForLog(payload);

        assertThat(redacted).contains("\"msg\":\"success\"");
        assertThat(redacted).contains("\"lenderResponse\"");
        assertThat(redacted).doesNotContain("原始报文");
        assertThat(redacted).contains("\"msg\":\"[redacted]\"");
    }

    @Test
    void redactsLenderDetailPrefixInRootMsg() {
        String payload = """
                {"code":"K000001","msg":"A000445: advanceAi OCR原始报文格式错误","data":null,"traceId":"t1"}
                """;

        String redacted = ApiLogPayloadSupport.redactForLog(payload);

        assertThat(redacted).contains("\"msg\":\"[redacted]\"");
        assertThat(redacted).doesNotContain("原始报文");
    }
}
