package com.pk.core.outbox;

public final class OutboxEventStatus {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    private OutboxEventStatus() {
    }
}
