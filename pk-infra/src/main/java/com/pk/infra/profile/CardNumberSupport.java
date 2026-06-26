package com.pk.infra.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

final class CardNumberSupport {
    static final String VERIFY_PASSED = "PASSED";

    private CardNumberSupport() {
    }

    static String normalize(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.trim();
    }

    static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    static String mask(String cardNumber) {
        String normalized = normalize(cardNumber);
        if (normalized.length() <= 4) {
            return "****" + normalized;
        }
        return "****" + normalized.substring(normalized.length() - 4);
    }
}
