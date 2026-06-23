package com.pk.app.api;

import com.pk.core.api.ApiCode;
import java.util.Objects;

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
        if (apiCode.success()) {
            throw new IllegalArgumentException("Success code cannot be used for failure responses");
        }
        return of(apiCode, null, traceId);
    }

    public static <T> ApiResponse<T> of(ApiCode apiCode, T data, String traceId) {
        Objects.requireNonNull(apiCode, "apiCode is required");
        return new ApiResponse<>(apiCode.code(), apiCode.message(), data, traceId);
    }
}
