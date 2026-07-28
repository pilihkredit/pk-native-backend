package com.pk.core.attribution.port;

import com.pk.core.callback.port.ServerEventCallbackParser;

public interface AppsFlyerS2sReporter {
    ReportResult report(long serverEventCallbackId, ServerEventCallbackParser.ParsedServerEventCallback event);

    record ReportResult(boolean reported, Long recordId, String message) {
        public static ReportResult skipped(String message) {
            return new ReportResult(false, null, message);
        }

        public static ReportResult recorded(long recordId, String message) {
            return new ReportResult(true, recordId, message);
        }
    }
}
