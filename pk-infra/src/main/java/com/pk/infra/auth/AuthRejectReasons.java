package com.pk.infra.auth;

/**
 * Request-scoped auth rejection reason for structured logging (not sent to clients).
 */
public final class AuthRejectReasons {
    public static final String TOKEN_NOT_PROVIDED = "token_not_provided";
    public static final String TOKEN_EXPIRED = "token_expired";
    public static final String SESSION_NOT_FOUND = "session_not_found";
    public static final String SESSION_VERSION_MISMATCH = "session_version_mismatch";
    public static final String ACCOUNT_CLOSED = "account_closed";
    public static final String TOKEN_REJECTED = "token_rejected";

    private static final ThreadLocal<String> REASON = new ThreadLocal<>();

    private AuthRejectReasons() {
    }

    public static void set(String reason) {
        if (reason == null || reason.isBlank()) {
            REASON.remove();
            return;
        }
        REASON.set(reason.trim());
    }

    public static String consume() {
        String reason = REASON.get();
        REASON.remove();
        return reason;
    }

    public static void clear() {
        REASON.remove();
    }
}
