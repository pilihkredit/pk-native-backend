package com.pk.infra.repay;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class RepayNoGenerator {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private RepayNoGenerator() {
    }

    static String trialNo() {
        return prefixed("RT");
    }

    static String batchTrialNo() {
        return prefixed("BT");
    }

    static String vaSnapshotNo() {
        return prefixed("VS");
    }

    static String currentOrderNo() {
        return prefixed("RCO");
    }

    private static String prefixed(String prefix) {
        String timestamp = LocalDateTime.now(ZONE).format(TIMESTAMP);
        StringBuilder suffix = new StringBuilder(6);
        for (int index = 0; index < 6; index++) {
            suffix.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return prefix + timestamp + suffix;
    }
}
