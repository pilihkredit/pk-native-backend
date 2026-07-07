package com.pk.app.debug.support;

import java.util.Base64;

public final class DebugImageDataUrlSupport {
    private DebugImageDataUrlSupport() {
    }

    public static String toDataUrl(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        return "data:" + detectMimeType(imageBytes) + ";base64,"
                + Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String detectMimeType(byte[] imageBytes) {
        if (imageBytes.length >= 3
                && (imageBytes[0] & 0xFF) == 0xFF
                && (imageBytes[1] & 0xFF) == 0xD8
                && (imageBytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (imageBytes.length >= 8
                && imageBytes[0] == (byte) 0x89
                && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E
                && imageBytes[3] == 0x47) {
            return "image/png";
        }
        if (imageBytes.length >= 3
                && imageBytes[0] == 0x47
                && imageBytes[1] == 0x49
                && imageBytes[2] == 0x46) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
