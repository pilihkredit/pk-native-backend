package com.pk.core.external;

import java.util.function.Supplier;

public final class LenderInteractionContext {
    private static final ThreadLocal<String> MOBILE_NO = new ThreadLocal<>();

    private LenderInteractionContext() {
    }

    public static String mobileNo() {
        return MOBILE_NO.get();
    }

    public static void setMobileNo(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            MOBILE_NO.remove();
            return;
        }
        MOBILE_NO.set(mobileNo.trim());
    }

    public static void clear() {
        MOBILE_NO.remove();
    }

    public static <T> T runWithMobileNo(String mobileNo, Supplier<T> action) {
        String previous = MOBILE_NO.get();
        try {
            setMobileNo(mobileNo);
            return action.get();
        } finally {
            if (previous == null || previous.isBlank()) {
                MOBILE_NO.remove();
            } else {
                MOBILE_NO.set(previous);
            }
        }
    }

    public static void runWithMobileNo(String mobileNo, Runnable action) {
        runWithMobileNo(mobileNo, () -> {
            action.run();
            return null;
        });
    }
}
