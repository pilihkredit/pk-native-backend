package com.pk.infra.ocr;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

final class AdvanceAiHttpSupport {
    private AdvanceAiHttpSupport() {
    }

    static String resolveEndpoint(OcrProperties properties, String pathOrUrl) {
        if (pathOrUrl != null && pathOrUrl.startsWith("http")) {
            return pathOrUrl;
        }
        String base = properties.baseUrl();
        if (base == null || base.isBlank()) {
            base = deriveBaseUrl(properties.accessTokenUrl());
        }
        return normalizeBaseUrl(base) + normalizePath(pathOrUrl);
    }

    private static String deriveBaseUrl(String accessTokenUrl) {
        if (accessTokenUrl == null || accessTokenUrl.isBlank()) {
            return "";
        }
        String marker = "/openapi/";
        int index = accessTokenUrl.indexOf(marker);
        if (index < 0) {
            return accessTokenUrl;
        }
        return accessTokenUrl.substring(0, index + "/openapi".length());
    }

    static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    static MultipartBody buildMultipartBody(Map<String, byte[]> fileFields) {
        String boundary = "----pkBoundary" + UUID.randomUUID();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (Map.Entry<String, byte[]> entry : fileFields.entrySet()) {
            appendLine(output, "--" + boundary);
            appendLine(output, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"image.jpg\"");
            appendLine(output, "Content-Type: image/jpeg");
            appendLine(output, "");
            output.writeBytes(entry.getValue());
            appendLine(output, "");
        }
        appendLine(output, "--" + boundary + "--");
        return new MultipartBody(boundary, output.toByteArray());
    }

    record MultipartBody(String boundary, byte[] body) {
    }

    private static void appendLine(ByteArrayOutputStream output, String line) {
        output.writeBytes(line.getBytes(StandardCharsets.UTF_8));
        output.write('\r');
        output.write('\n');
    }
}
