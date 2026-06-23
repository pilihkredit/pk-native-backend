package com.pk.core.api;

import java.util.Objects;

public class ApiException extends RuntimeException {
    private final ApiCode apiCode;

    public ApiException(ApiCode apiCode) {
        this(apiCode, null);
    }

    public ApiException(ApiCode apiCode, Throwable cause) {
        super(requireFailureCode(apiCode).message(), cause);
        this.apiCode = apiCode;
    }

    public ApiCode apiCode() {
        return apiCode;
    }

    private static ApiCode requireFailureCode(ApiCode apiCode) {
        Objects.requireNonNull(apiCode, "apiCode is required");
        if (apiCode.success()) {
            throw new IllegalArgumentException("Success code cannot be used for exceptions");
        }
        return apiCode;
    }
}
