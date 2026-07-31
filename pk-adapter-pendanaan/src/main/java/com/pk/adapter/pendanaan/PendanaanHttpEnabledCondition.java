package com.pk.adapter.pendanaan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class PendanaanHttpEnabledCondition implements Condition {
    private static final Logger log = LoggerFactory.getLogger(PendanaanHttpEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        PendanaanProperties properties = Binder.get(context.getEnvironment())
                .bind("pk.lender.pendanaan", Bindable.of(PendanaanProperties.class))
                .orElseGet(PendanaanProperties::new);
        if (!properties.httpEnabled()) {
            return false;
        }
        if (properties.httpCredentialsPresent()) {
            return true;
        }
        log.warn(
                "pk.lender.pendanaan.mode=http but base-url/client-id/client-secret/app-name are incomplete; "
                        + "using fake Pendanaan adapters"
        );
        return false;
    }
}
