package com.pk.app.common.web;

import com.pk.infra.ocr.OcrLogSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

final class ApiHttpPayloadFormatter {
    private ApiHttpPayloadFormatter() {
    }

    static String formatBody(byte[] body, String contentType, int maxBodyBytes) {
        return formatBody(body, contentType, maxBodyBytes, false);
    }

    static String formatBody(byte[] body, String contentType, int maxBodyBytes, boolean redactOcrFields) {
        if (body == null || body.length == 0) {
            return "";
        }
        if (!isTextLike(contentType)) {
            return "[binary contentType=" + sanitize(contentType) + " length=" + body.length + "]";
        }
        int length = Math.min(body.length, maxBodyBytes);
        String text = new String(body, 0, length, resolveCharset(contentType));
        if (body.length > maxBodyBytes) {
            text = text + "...[truncated " + (body.length - maxBodyBytes) + " bytes]";
        }
        if (redactOcrFields) {
            return OcrLogSupport.redactPayload(text);
        }
        return text;
    }

    static String formatQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            return queryString;
        }
        if (request.getParameterMap().isEmpty()) {
            return "";
        }
        return request.getParameterMap().entrySet().stream()
                .flatMap(entry -> java.util.Arrays.stream(entry.getValue())
                        .map(value -> entry.getKey() + "=" + value))
                .collect(Collectors.joining("&"));
    }

    static Map<String, String> formatHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return headers;
        }
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name == null || name.isBlank()) {
                continue;
            }
            headers.put(name, redactHeaderValue(name, request.getHeader(name)));
        }
        return headers;
    }

    private static String redactHeaderValue(String name, String value) {
        if (value == null) {
            return "";
        }
        String sanitized = sanitize(value);
        if (isSensitiveHeader(name)) {
            if (sanitized.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return "Bearer ***";
            }
            return "***";
        }
        return sanitized;
    }

    private static boolean isSensitiveHeader(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.equals("authorization")
                || lower.equals("cookie")
                || lower.equals("set-cookie")
                || lower.equals("x-api-key")
                || lower.equals("proxy-authorization");
    }

    private static boolean isTextLike(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true;
        }
        String lower = contentType.toLowerCase();
        return lower.contains("json")
                || lower.contains("text")
                || lower.contains("xml")
                || lower.contains("x-www-form-urlencoded");
    }

    private static Charset resolveCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        int charsetIndex = contentType.toLowerCase().indexOf("charset=");
        if (charsetIndex < 0) {
            return StandardCharsets.UTF_8;
        }
        String charsetName = contentType.substring(charsetIndex + "charset=".length()).trim();
        int terminator = charsetName.indexOf(';');
        if (terminator >= 0) {
            charsetName = charsetName.substring(0, terminator).trim();
        }
        try {
            return Charset.forName(charsetName);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }
}
