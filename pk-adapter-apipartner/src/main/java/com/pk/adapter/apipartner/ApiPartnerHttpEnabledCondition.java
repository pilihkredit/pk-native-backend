package com.pk.adapter.apipartner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class ApiPartnerHttpEnabledCondition implements Condition {
    private static final Logger log = LoggerFactory.getLogger(ApiPartnerHttpEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ApiPartnerProperties properties = Binder.get(context.getEnvironment())
                .bind("pk.lender.apipartner", Bindable.of(ApiPartnerProperties.class))
                .orElseGet(ApiPartnerProperties::new);
        if (!properties.httpEnabled()) {
            return false;
        }
        if (properties.httpCredentialsPresent()) {
            return true;
        }
        log.warn(
                "pk.lender.apipartner.mode=http but base-url/client-id/client-secret/app-name are incomplete; "
                        + "using fake ApiPartner adapters"
        );
        return false;
    }
}
