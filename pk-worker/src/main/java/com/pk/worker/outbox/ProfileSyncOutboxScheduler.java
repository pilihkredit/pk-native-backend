package com.pk.worker.outbox;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.profile.ProfileSyncHandler;
import com.pk.infra.profile.ProfileSyncJob;
import com.pk.infra.profile.ProfileSyncOutboxPublisher;
import com.pk.infra.profile.ProfileSyncProperties;
import com.pk.worker.config.WorkerOutboxProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProfileSyncOutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(ProfileSyncOutboxScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final OutboxEventRepository outboxEventRepository;
    private final ProfileSyncOutboxPublisher profileSyncOutboxPublisher;
    private final ProfileSyncHandler profileSyncHandler;
    private final ProfileSyncProperties profileSyncProperties;

    public ProfileSyncOutboxScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            OutboxEventRepository outboxEventRepository,
            ProfileSyncOutboxPublisher profileSyncOutboxPublisher,
            ProfileSyncHandler profileSyncHandler,
            ProfileSyncProperties profileSyncProperties
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.outboxEventRepository = outboxEventRepository;
        this.profileSyncOutboxPublisher = profileSyncOutboxPublisher;
        this.profileSyncHandler = profileSyncHandler;
        this.profileSyncProperties = profileSyncProperties;
    }

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<OutboxEvent> events = outboxEventRepository.findReady(workerOutboxProperties.batchSize());
        for (OutboxEvent event : events) {
            if (!OutboxEventTypes.PROFILE_SYNC.equals(event.eventType())) {
                continue;
            }
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        try {
            ProfileSyncJob job = profileSyncOutboxPublisher.deserialize(event.payloadJson());
            profileSyncHandler.sync(job);
            outboxEventRepository.markCompleted(event.id());
        } catch (RuntimeException exception) {
            int nextRetry = event.retryCount() + 1;
            if (nextRetry >= profileSyncProperties.maxRetries()) {
                log.warn("Profile sync outbox event {} failed after {} attempts", event.eventNo(), nextRetry, exception);
                outboxEventRepository.markTerminalFailure(event.id(), nextRetry);
                return;
            }
            log.debug("Profile sync outbox event {} failed, scheduling retry {}", event.eventNo(), nextRetry, exception);
            outboxEventRepository.markFailed(
                    event.id(),
                    nextRetry,
                    Instant.now().plusSeconds(profileSyncProperties.retryBackoffSeconds())
            );
        }
    }
}
