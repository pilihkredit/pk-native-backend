package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import java.util.Objects;

/**
 * Standard API response envelope.
 *
 * @param code    business result code; 000000 means success
 * @param msg     human-readable message in English
 * @param data    payload on success; null on failure
 * @param traceId request trace id; echoes X-Trace-Id header when provided
 */
public record ApiResponse<T>(
        String code,
        String msg,
        T data,
        String traceId
) {
    public ApiResponse {
        Objects.requireNonNull(code, "code is required");
        Objects.requireNonNull(msg, "msg is required");
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return of(ApiCode.SUCCESS, data, traceId);
    }

    public static <T> ApiResponse<T> failure(ApiCode apiCode, String traceId) {
        return failure(apiCode, apiCode.message(), traceId);
    }

    public static <T> ApiResponse<T> failure(ApiCode apiCode, String message, String traceId) {
        if (apiCode.success()) {
            throw new IllegalArgumentException("Success code cannot be used for failure responses");
        }
        String resolvedMessage = message == null || message.isBlank() ? apiCode.message() : message.trim();
        return new ApiResponse<>(apiCode.code(), resolvedMessage, null, traceId);
    }

    public static <T> ApiResponse<T> of(ApiCode apiCode, T data, String traceId) {
        Objects.requireNonNull(apiCode, "apiCode is required");
        return new ApiResponse<>(apiCode.code(), apiCode.message(), data, traceId);
    }
}
