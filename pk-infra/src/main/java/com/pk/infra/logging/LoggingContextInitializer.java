package com.pk.infra.logging;

import com.pk.core.logging.LogContext;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class LoggingContextInitializer {
    private final LoggingRuntimeContext runtimeContext;

    public LoggingContextInitializer(LoggingRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    @PostConstruct
    void init() {
        LogContext.putService(runtimeContext.service());
        LogContext.putEnvironment(runtimeContext.environment());
    }
}
