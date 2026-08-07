package com.pk.core.external;

import java.util.function.Supplier;

public final class LenderInteractionContext {
    private static final ThreadLocal<String> MOBILE_NO = new ThreadLocal<>();
    private static final ThreadLocal<String> SOURCE = new ThreadLocal<>();

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

    public static String source() {
        String value = SOURCE.get();
        if (value == null || value.isBlank()) {
            return DataWriteSource.APP;
        }
        return value;
    }

    public static void setSource(String source) {
        if (source == null || source.isBlank()) {
            SOURCE.remove();
            return;
        }
        SOURCE.set(source.trim());
    }

    public static void clear() {
        MOBILE_NO.remove();
        SOURCE.remove();
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

    public static <T> T runWith(String mobileNo, String source, Supplier<T> action) {
        String previousMobile = MOBILE_NO.get();
        String previousSource = SOURCE.get();
        try {
            setMobileNo(mobileNo);
            setSource(source);
            return action.get();
        } finally {
            if (previousMobile == null || previousMobile.isBlank()) {
                MOBILE_NO.remove();
            } else {
                MOBILE_NO.set(previousMobile);
            }
            if (previousSource == null || previousSource.isBlank()) {
                SOURCE.remove();
            } else {
                SOURCE.set(previousSource);
            }
        }
    }

    public static void runWith(String mobileNo, String source, Runnable action) {
        runWith(mobileNo, source, () -> {
            action.run();
            return null;
        });
    }
}
