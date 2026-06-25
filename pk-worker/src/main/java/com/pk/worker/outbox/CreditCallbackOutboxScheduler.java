package com.pk.worker.outbox;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.credit.CreditApplyProperties;
import com.pk.infra.credit.CreditCallbackHandler;
import com.pk.infra.credit.CreditCallbackJob;
import com.pk.infra.credit.CreditCallbackOutboxPublisher;
import com.pk.worker.config.WorkerOutboxProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CreditCallbackOutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(CreditCallbackOutboxScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final OutboxEventRepository outboxEventRepository;
    private final CreditCallbackOutboxPublisher creditCallbackOutboxPublisher;
    private final CreditCallbackHandler creditCallbackHandler;
    private final CreditApplyProperties creditApplyProperties;

    public CreditCallbackOutboxScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            OutboxEventRepository outboxEventRepository,
            CreditCallbackOutboxPublisher creditCallbackOutboxPublisher,
            CreditCallbackHandler creditCallbackHandler,
            CreditApplyProperties creditApplyProperties
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.outboxEventRepository = outboxEventRepository;
        this.creditCallbackOutboxPublisher = creditCallbackOutboxPublisher;
        this.creditCallbackHandler = creditCallbackHandler;
        this.creditApplyProperties = creditApplyProperties;
    }

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<OutboxEvent> events = outboxEventRepository.findReady(workerOutboxProperties.batchSize());
        for (OutboxEvent event : events) {
            if (!OutboxEventTypes.CREDIT_CALLBACK.equals(event.eventType())) {
                continue;
            }
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            CreditCallbackJob job = creditCallbackOutboxPublisher.deserialize(event.payloadJson());
            creditCallbackHandler.handle(job);
            outboxEventRepository.markCompleted(event.id());
        } catch (RuntimeException exception) {
            int nextRetry = event.retryCount() + 1;
            if (nextRetry >= creditApplyProperties.maxRetries()) {
                log.warn("Credit callback outbox event {} failed after {} attempts", event.eventNo(), nextRetry, exception);
                outboxEventRepository.markTerminalFailure(event.id(), nextRetry);
                return;
            }
            log.debug("Credit callback outbox event {} failed, scheduling retry {}", event.eventNo(), nextRetry, exception);
            outboxEventRepository.markFailed(
                    event.id(),
                    nextRetry,
                    Instant.now().plusSeconds(creditApplyProperties.retryBackoffSeconds())
            );
        }
    }
}
