package com.pk.infra.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.aliyun.openservices.log.common.LogItem;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoghubAppenderLogOnlyTest {
    @Test
    void flattensStandardSchemaFieldsOntoLogItem() {
        LogItem item = new LogItem(1_700_000_000);
        String json = """
                {
                  "timestamp":"2026-07-13T08:00:00Z",
                  "level":"INFO",
                  "service":"pk-app",
                  "environment":"prod",
                  "traceId":"trace-1",
                  "message":"api.access",
                  "uri":"/api/v1/credit/apply",
                  "method":"POST",
                  "status":200,
                  "durationMs":156,
                  "code":"000000"
                }
                """;

        LoghubAppenderLogOnly.pushJsonFields(item, json);

        Map<String, String> fields = toMap(item);
        assertThat(fields.get("timestamp")).isEqualTo("2026-07-13T08:00:00Z");
        assertThat(fields.get("level")).isEqualTo("INFO");
        assertThat(fields.get("service")).isEqualTo("pk-app");
        assertThat(fields.get("environment")).isEqualTo("prod");
        assertThat(fields.get("traceId")).isEqualTo("trace-1");
        assertThat(fields.get("uri")).isEqualTo("/api/v1/credit/apply");
        assertThat(fields.get("method")).isEqualTo("POST");
        assertThat(fields.get("status")).isEqualTo("200");
        assertThat(fields.get("durationMs")).isEqualTo("156");
        assertThat(fields.get("code")).isEqualTo("000000");
        assertThat(fields).doesNotContainKey("log");
    }

    @Test
    void fallsBackToLogFieldWhenJsonInvalid() {
        LogItem item = new LogItem(1_700_000_000);
        LoghubAppenderLogOnly.pushJsonFields(item, "not-json");
        Map<String, String> fields = toMap(item);
        assertThat(fields.get("log")).isEqualTo("not-json");
    }

    private static Map<String, String> toMap(LogItem item) {
        Map<String, String> fields = new HashMap<>();
        for (int index = 0; index < item.GetLogContents().size(); index++) {
            fields.put(item.GetLogContents().get(index).GetKey(), item.GetLogContents().get(index).GetValue());
        }
        return fields;
    }
}
