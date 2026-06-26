package com.pk.core.outbox;

public final class OutboxEventTypes {
    public static final String PROFILE_SYNC = "PROFILE_SYNC";
    public static final String CREDIT_APPLY = "CREDIT_APPLY";
    public static final String CREDIT_CALLBACK = "CREDIT_CALLBACK";
    public static final String LOAN_APPLY = "LOAN_APPLY";
    public static final String LOAN_CALLBACK = "LOAN_CALLBACK";

    private OutboxEventTypes() {
    }
}
