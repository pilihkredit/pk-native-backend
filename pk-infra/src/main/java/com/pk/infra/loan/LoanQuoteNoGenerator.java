package com.pk.infra.loan;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class LoanQuoteNoGenerator {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private LoanQuoteNoGenerator() {
    }

    static String generate() {
        String timestamp = LocalDateTime.now(ZONE).format(TIMESTAMP);
        StringBuilder suffix = new StringBuilder(6);
        for (int index = 0; index < 6; index++) {
            suffix.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return "QUOTE" + timestamp + suffix;
    }
}
