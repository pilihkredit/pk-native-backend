package com.pk.worker.outbox;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.credit.CreditApplyHandler;
import com.pk.infra.credit.CreditApplyJob;
import com.pk.infra.credit.CreditApplyOutboxPublisher;
import com.pk.infra.credit.CreditApplyProperties;
import com.pk.worker.config.WorkerOutboxProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CreditApplyOutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(CreditApplyOutboxScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final OutboxEventRepository outboxEventRepository;
    private final CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    private final CreditApplyHandler creditApplyHandler;
    private final CreditApplyProperties creditApplyProperties;

    public CreditApplyOutboxScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            OutboxEventRepository outboxEventRepository,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditApplyHandler creditApplyHandler,
            CreditApplyProperties creditApplyProperties
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.outboxEventRepository = outboxEventRepository;
        this.creditApplyOutboxPublisher = creditApplyOutboxPublisher;
        this.creditApplyHandler = creditApplyHandler;
        this.creditApplyProperties = creditApplyProperties;
    }

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<OutboxEvent> events = outboxEventRepository.findReady(workerOutboxProperties.batchSize());
        for (OutboxEvent event : events) {
            if (!OutboxEventTypes.CREDIT_APPLY.equals(event.eventType())) {
                continue;
            }
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            CreditApplyJob job = creditApplyOutboxPublisher.deserialize(event.payloadJson());
            creditApplyHandler.submit(job);
            outboxEventRepository.markCompleted(event.id());
        } catch (RuntimeException exception) {
            int nextRetry = event.retryCount() + 1;
            if (nextRetry >= creditApplyProperties.maxRetries()) {
                log.warn("Credit apply outbox event {} failed after {} attempts", event.eventNo(), nextRetry, exception);
                outboxEventRepository.markTerminalFailure(event.id(), nextRetry);
                return;
            }
            log.debug("Credit apply outbox event {} failed, scheduling retry {}", event.eventNo(), nextRetry, exception);
            outboxEventRepository.markFailed(
                    event.id(),
                    nextRetry,
                    Instant.now().plusSeconds(creditApplyProperties.retryBackoffSeconds())
            );
        }
    }
}
