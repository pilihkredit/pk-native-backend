package com.pk.infra.auth;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public final class OtpCodeGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpCodeGenerator() {
    }

    public static String sixDigits() {
        return numericCode(6);
    }

    /**
     * Numeric OTP with fixed length (no leading zero), aligned with pk-credit-core codeLength.
     */
    public static String numericCode(int length) {
        int digits = Math.max(4, Math.min(8, length));
        int bound = (int) Math.pow(10, digits);
        int min = bound / 10;
        int value = SECURE_RANDOM.nextInt(bound - min) + min;
        return Integer.toString(value);
    }

    public static String token() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong())
                + Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    public static String refreshTokenId() {
        return "rt_" + token();
    }
}
