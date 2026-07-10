package com.pk.infra.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggingRuntimeContext {
    private final String service;
    private final String environment;

    public LoggingRuntimeContext(
            @Value("${spring.application.name:pk-app}") String service,
            @Value("${pk.environment:local}") String environment
    ) {
        this.service = service;
        this.environment = environment;
    }

    public String service() {
        return service;
    }

    public String environment() {
        return environment;
    }
}
