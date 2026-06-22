package com.pk.core.error;

public class AppBusinessException extends RuntimeException {
    private final String code;

    public AppBusinessException(String code) {
        super(ErrorMessages.forCode(code));
        this.code = code;
    }

    public String code() {
        return code;
    }

    public String msg() {
        return getMessage();
    }
}
