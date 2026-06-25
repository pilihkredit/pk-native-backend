package com.pk.worker.reference;

import com.pk.infra.reference.ReferenceRefreshService;
import com.pk.worker.config.WorkerReferenceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReferenceRefreshScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReferenceRefreshScheduler.class);

    private final ReferenceRefreshService referenceRefreshService;
    private final WorkerReferenceProperties properties;

    public ReferenceRefreshScheduler(
            ReferenceRefreshService referenceRefreshService,
            WorkerReferenceProperties properties
    ) {
        this.referenceRefreshService = referenceRefreshService;
        this.properties = properties;
    }

    @Scheduled(cron = "${pk.worker.reference.bank-refresh-cron:0 0 3 * * *}", zone = "${pk.worker.reference.refresh-zone:Asia/Jakarta}")
    public void refreshBanks() {
        if (!properties.enabled()) {
            return;
        }
        try {
            referenceRefreshService.refreshBanks();
        } catch (RuntimeException exception) {
            log.error("Scheduled bank reference refresh failed", exception);
        }
    }

    @Scheduled(cron = "${pk.worker.reference.area-refresh-cron:0 30 3 * * *}", zone = "${pk.worker.reference.refresh-zone:Asia/Jakarta}")
    public void refreshAreas() {
        if (!properties.enabled()) {
            return;
        }
        try {
            referenceRefreshService.refreshAreas();
        } catch (RuntimeException exception) {
            log.error("Scheduled area reference refresh failed", exception);
        }
    }
}
