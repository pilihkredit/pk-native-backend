package com.pk.adapter.apipartner;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class ApiPartnerHttpDisabledCondition implements Condition {
    private final ApiPartnerHttpEnabledCondition httpEnabledCondition = new ApiPartnerHttpEnabledCondition();

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !httpEnabledCondition.matches(context, metadata);
    }
}
