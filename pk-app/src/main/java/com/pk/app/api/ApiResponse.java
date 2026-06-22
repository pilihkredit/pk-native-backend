package com.pk.app.api;

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
        return new ApiResponse<>("000000", "success", data, traceId);
    }
}
