package com.pk.infra.profile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class EktpValidator {
    private EktpValidator() {
    }

    public static boolean isValid(String idNo) {
        if (idNo == null) {
            return false;
        }
        String normalized = idNo.trim();
        if (normalized.length() != 16) {
            return false;
        }
        for (int index = 0; index < normalized.length(); index++) {
            if (!Character.isDigit(normalized.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    public static String hash(String idNo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(idNo.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
