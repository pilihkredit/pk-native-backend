package com.pk.infra.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.logback.LoghubAppender;
import com.aliyun.openservices.log.logback.LoghubAppenderCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SLS appender that flattens encoder JSON into top-level LogItem fields.
 * Official {@link LoghubAppender} typically wraps the payload (e.g. under {@code log}),
 * which breaks platform indexing on {@code uri}/{@code status}/{@code durationMs}/{@code code}.
 */
@SuppressWarnings("rawtypes")
public class LoghubAppenderLogOnly extends LoghubAppender {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void append(Object eventObject) {
        try {
            appendFlattened(eventObject);
        } catch (Exception exception) {
            addError("Failed to append event to SLS.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void appendFlattened(Object eventObject) throws ProducerException, InterruptedException {
        if (!(eventObject instanceof ILoggingEvent event)) {
            return;
        }
        Encoder enc = getEncoder();
        if (enc == null) {
            return;
        }

        String json = new String(enc.encode(eventObject), StandardCharsets.UTF_8);
        List<LogItem> logItems = new ArrayList<>();
        LogItem item = new LogItem((int) (event.getTimeStamp() / 1000));
        logItems.add(item);
        pushJsonFields(item, json);

        long deadlineMs = System.currentTimeMillis() + getMaxBlockMs();
        do {
            try {
                producer.send(
                        projectConfig.getProject(),
                        logStore,
                        topic,
                        source,
                        logItems,
                        new LoghubAppenderCallback(
                                this,
                                projectConfig.getProject(),
                                logStore,
                                topic,
                                source,
                                logItems
                        )
                );
                break;
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception exception) {
                addError("Failed to send log to SLS.", exception);
                break;
            }
        } while (System.currentTimeMillis() < deadlineMs);
    }

    static void pushJsonFields(LogItem item, String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, Map.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.isBlank()) {
                    continue;
                }
                Object value = entry.getValue();
                String stringValue;
                if (value == null) {
                    stringValue = "";
                } else if (value instanceof String text) {
                    stringValue = text;
                } else {
                    stringValue = OBJECT_MAPPER.writeValueAsString(value);
                }
                item.PushBack(key, stringValue);
            }
        } catch (Exception exception) {
            item.PushBack("log", json == null ? "" : json);
        }
    }
}
