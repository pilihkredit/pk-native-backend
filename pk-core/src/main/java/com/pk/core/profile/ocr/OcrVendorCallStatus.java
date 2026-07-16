package com.pk.core.profile.ocr;

/**
 * Status values for ocr_vendor_call_log.status.
 */
public enum OcrVendorCallStatus {
    SUCCESS,
    VENDOR_ERROR,
    /** @deprecated no longer written; kept for historical rows */
    @Deprecated
    BIZ_REJECT,
    TIMEOUT,
    EXCEPTION
}
