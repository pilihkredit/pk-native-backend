package com.pk.core.profile.ocr;

/**
 * Request-scoped context for OCR vendor call auditing.
 */
public final class OcrCallContextHolder {
    private static final ThreadLocal<OcrCallContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Long> LAST_VENDOR_CALL_LOG_ID = new ThreadLocal<>();

    private OcrCallContextHolder() {
    }

    public static void set(OcrCallContext context) {
        CONTEXT.set(context);
        LAST_VENDOR_CALL_LOG_ID.remove();
    }

    public static OcrCallContext get() {
        return CONTEXT.get();
    }

    public static void setLastVendorCallLogId(long id) {
        if (id > 0) {
            LAST_VENDOR_CALL_LOG_ID.set(id);
        }
    }

    public static Long lastVendorCallLogId() {
        return LAST_VENDOR_CALL_LOG_ID.get();
    }

    public static void clear() {
        CONTEXT.remove();
        LAST_VENDOR_CALL_LOG_ID.remove();
    }
}
