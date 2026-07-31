package com.pk.adapter.pendanaan;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class PendanaanHttpDisabledCondition implements Condition {
    private final PendanaanHttpEnabledCondition httpEnabledCondition = new PendanaanHttpEnabledCondition();

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !httpEnabledCondition.matches(context, metadata);
    }
}
