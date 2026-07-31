package com.pk.core.profile.ocr;

/**
 * Vendor OCR call operation types persisted in ocr_vendor_call_log.
 */
public enum OcrVendorOperationType {
    LICENSE_TOKEN,
    OCR_CHECK,
    LIVENESS_CHECK,
    LIVENESS_LICENSE,
    LIVENESS_RESULT,
    FACE_COMPARE
}
