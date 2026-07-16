package com.pk.core.profile.ocr;

public final class OcrCallContextHolder {
    private static final ThreadLocal<OcrCallContext> HOLDER = new ThreadLocal<>();

    private OcrCallContextHolder() {
    }

    public static void set(OcrCallContext context) {
        HOLDER.set(context);
    }

    public static OcrCallContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
