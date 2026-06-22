package com.pk.worker.outbox;

import com.pk.core.task.WorkerQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "pk.worker.outbox.enabled", havingValue = "true")
public class OutboxPoller {
    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    @Scheduled(fixedDelayString = "${pk.worker.outbox.poll-interval-ms:5000}")
    public void poll() {
        for (WorkerQueue queue : WorkerQueue.values()) {
            log.debug("outbox poll queue={}", queue);
        }
    }
}
