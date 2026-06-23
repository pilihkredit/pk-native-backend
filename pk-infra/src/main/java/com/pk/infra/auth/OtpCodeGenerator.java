package com.pk.infra.auth;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public final class OtpCodeGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OtpCodeGenerator() {
    }

    public static String sixDigits() {
        int value = SECURE_RANDOM.nextInt(900_000) + 100_000;
        return Integer.toString(value);
    }

    public static String token() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong()) + Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    public static String refreshTokenId() {
        return "rt_" + token();
    }
}
