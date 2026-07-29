package com.pk.core.attribution.port;

import com.pk.core.callback.port.ServerEventCallbackParser;

public interface AppsFlyerS2sReporter {
    String EVENT_REGISTER_SUCCESS_PK = "REGISTER_SUCCESS_PK";

    ReportResult report(Long serverEventCallbackId, ServerEventCallbackParser.ParsedServerEventCallback event);

    /**
     * Platform-originated event (no lender callback row), e.g. local registration success.
     */
    ReportResult reportPlatformEvent(
            String eventType,
            long userId,
            String partnerUserId,
            String deviceNo,
            String systemPlatform,
            String adId
    );

    record ReportResult(boolean reported, Long recordId, String message) {
        public static ReportResult skipped(String message) {
            return new ReportResult(false, null, message);
        }

        public static ReportResult skipped(long recordId, String message) {
            return new ReportResult(false, recordId, message);
        }

        public static ReportResult recorded(long recordId, String message) {
            return new ReportResult(true, recordId, message);
        }
    }
}
