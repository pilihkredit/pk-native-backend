package com.pk.worker.outbox;

import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.loan.LoanApplyHandler;
import com.pk.infra.loan.LoanApplyJob;
import com.pk.infra.loan.LoanApplyOutboxPublisher;
import com.pk.infra.loan.LoanApplyProperties;
import com.pk.infra.logging.LoggingRuntimeContext;
import com.pk.infra.logging.OutboxStructuredLogging;
import com.pk.infra.logging.TaskTraceSupport;
import com.pk.worker.config.WorkerOutboxProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(LoanApplyHandler.class)
public class LoanApplyOutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(LoanApplyOutboxScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final OutboxEventRepository outboxEventRepository;
    private final LoanApplyOutboxPublisher loanApplyOutboxPublisher;
    private final LoanApplyHandler loanApplyHandler;
    private final LoanApplyProperties loanApplyProperties;
    private final PlatformStructuredLogger structuredLogger;
    private final LoggingRuntimeContext loggingRuntimeContext;

    public LoanApplyOutboxScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            OutboxEventRepository outboxEventRepository,
            LoanApplyOutboxPublisher loanApplyOutboxPublisher,
            LoanApplyHandler loanApplyHandler,
            LoanApplyProperties loanApplyProperties,
            PlatformStructuredLogger structuredLogger,
            LoggingRuntimeContext loggingRuntimeContext
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.outboxEventRepository = outboxEventRepository;
        this.loanApplyOutboxPublisher = loanApplyOutboxPublisher;
        this.loanApplyHandler = loanApplyHandler;
        this.loanApplyProperties = loanApplyProperties;
        this.structuredLogger = structuredLogger;
        this.loggingRuntimeContext = loggingRuntimeContext;
    }

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        TaskTraceSupport.run(loggingRuntimeContext, "outbox.loan-apply", this::pollInternal);
    }

    private void pollInternal() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<OutboxEvent> events = outboxEventRepository.findReady(workerOutboxProperties.batchSize());
        for (OutboxEvent event : events) {
            if (!OutboxEventTypes.LOAN_APPLY.equals(event.eventType())) {
                continue;
            }
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            LoanApplyJob job = loanApplyOutboxPublisher.deserialize(event.payloadJson());
            loanApplyHandler.submit(job);
            outboxEventRepository.markCompleted(event.id());
        } catch (RuntimeException exception) {
            int nextRetry = event.retryCount() + 1;
            if (nextRetry >= loanApplyProperties.maxRetries()) {
                log.warn("Loan apply outbox event {} failed after {} attempts", event.eventNo(), nextRetry, exception);
                OutboxStructuredLogging.logTerminalFailure(structuredLogger, event, nextRetry, exception);
                outboxEventRepository.markTerminalFailure(event.id(), nextRetry);
                return;
            }
            log.debug("Loan apply outbox event {} failed, scheduling retry {}", event.eventNo(), nextRetry, exception);
            outboxEventRepository.markFailed(
                    event.id(),
                    nextRetry,
                    Instant.now().plusSeconds(loanApplyProperties.retryBackoffSeconds())
            );
        }
    }
}
