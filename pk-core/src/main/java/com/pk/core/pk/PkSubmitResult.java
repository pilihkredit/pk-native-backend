package com.pk.core.pk;

import java.util.Objects;

public record PkSubmitResult(
        boolean accepted,
        String externalId,
        String message
) {
    public PkSubmitResult {
        Objects.requireNonNull(externalId, "externalId is required");
        Objects.requireNonNull(message, "message is required");
    }

    public static PkSubmitResult accepted(String externalId) {
        return new PkSubmitResult(true, externalId, "accepted");
    }

    public static PkSubmitResult rejected(String externalId, String message) {
        return new PkSubmitResult(false, externalId, message);
    }
}
