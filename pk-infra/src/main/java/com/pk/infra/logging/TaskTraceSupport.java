package com.pk.infra.logging;

import com.pk.core.logging.LogContext;
import java.util.UUID;

public final class TaskTraceSupport {
    private TaskTraceSupport() {
    }

    public static void run(LoggingRuntimeContext runtimeContext, String taskName, Runnable runnable) {
        LogContext.putTraceId(UUID.randomUUID().toString());
        LogContext.putService(runtimeContext.service());
        LogContext.putEnvironment(runtimeContext.environment());
        try {
            runnable.run();
        } finally {
            LogContext.clear();
        }
    }
}
