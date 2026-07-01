package com.pk.core.api;

import java.util.Objects;

public class ApiException extends RuntimeException {
    private final ApiCode apiCode;
    private final String detail;

    public ApiException(ApiCode apiCode) {
        this(apiCode, null, null);
    }

    public ApiException(ApiCode apiCode, String detail) {
        this(apiCode, detail, null);
    }

    public ApiException(ApiCode apiCode, Throwable cause) {
        this(apiCode, null, cause);
    }

    private ApiException(ApiCode apiCode, String detail, Throwable cause) {
        super(buildMessage(requireFailureCode(apiCode), detail), cause);
        this.apiCode = apiCode;
        this.detail = normalizeDetail(detail);
    }

    public ApiCode apiCode() {
        return apiCode;
    }

    public String detail() {
        return detail;
    }

    private static ApiCode requireFailureCode(ApiCode apiCode) {
        Objects.requireNonNull(apiCode, "apiCode is required");
        if (apiCode.success()) {
            throw new IllegalArgumentException("Success code cannot be used for exceptions");
        }
        return apiCode;
    }

    private static String buildMessage(ApiCode apiCode, String detail) {
        String normalizedDetail = normalizeDetail(detail);
        if (normalizedDetail == null) {
            return apiCode.message();
        }
        return apiCode.message() + ": " + normalizedDetail;
    }

    private static String normalizeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return null;
        }
        return detail.trim();
    }
}
