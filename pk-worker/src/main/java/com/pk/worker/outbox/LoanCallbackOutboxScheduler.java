package com.pk.worker.outbox;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.loan.LoanApplyProperties;
import com.pk.infra.loan.LoanCallbackHandler;
import com.pk.infra.loan.LoanCallbackJob;
import com.pk.infra.loan.LoanCallbackOutboxPublisher;
import com.pk.worker.config.WorkerOutboxProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(LoanCallbackHandler.class)
public class LoanCallbackOutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(LoanCallbackOutboxScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final OutboxEventRepository outboxEventRepository;
    private final LoanCallbackOutboxPublisher loanCallbackOutboxPublisher;
    private final LoanCallbackHandler loanCallbackHandler;
    private final LoanApplyProperties loanApplyProperties;

    public LoanCallbackOutboxScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            OutboxEventRepository outboxEventRepository,
            LoanCallbackOutboxPublisher loanCallbackOutboxPublisher,
            LoanCallbackHandler loanCallbackHandler,
            LoanApplyProperties loanApplyProperties
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.outboxEventRepository = outboxEventRepository;
        this.loanCallbackOutboxPublisher = loanCallbackOutboxPublisher;
        this.loanCallbackHandler = loanCallbackHandler;
        this.loanApplyProperties = loanApplyProperties;
    }

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<OutboxEvent> events = outboxEventRepository.findReady(workerOutboxProperties.batchSize());
        for (OutboxEvent event : events) {
            if (!OutboxEventTypes.LOAN_CALLBACK.equals(event.eventType())) {
                continue;
            }
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            LoanCallbackJob job = loanCallbackOutboxPublisher.deserialize(event.payloadJson());
            loanCallbackHandler.handle(job);
            outboxEventRepository.markCompleted(event.id());
        } catch (RuntimeException exception) {
            int nextRetry = event.retryCount() + 1;
            if (nextRetry >= loanApplyProperties.maxRetries()) {
                log.warn("Loan callback outbox event {} failed after {} attempts", event.eventNo(), nextRetry, exception);
                outboxEventRepository.markTerminalFailure(event.id(), nextRetry);
                return;
            }
            log.debug("Loan callback outbox event {} failed, scheduling retry {}", event.eventNo(), nextRetry, exception);
            outboxEventRepository.markFailed(
                    event.id(),
                    nextRetry,
                    Instant.now().plusSeconds(loanApplyProperties.retryBackoffSeconds())
            );
        }
    }
}
