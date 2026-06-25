package com.pk.worker.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.infra.credit.CreditStatusPollHandler;
import com.pk.worker.config.WorkerOutboxProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CreditStatusPollScheduler {
    private static final Logger log = LoggerFactory.getLogger(CreditStatusPollScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusPollHandler creditStatusPollHandler;

    public CreditStatusPollScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusPollHandler creditStatusPollHandler
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusPollHandler = creditStatusPollHandler;
    }

    @Scheduled(fixedDelayString = "${pk.worker.credit.poll-interval-ms:30000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<CreditApplicationRepository.CreditApplicationRecord> due =
                creditApplicationRepository.findDueForPoll(workerOutboxProperties.batchSize());
        for (CreditApplicationRepository.CreditApplicationRecord record : due) {
            try {
                creditStatusPollHandler.poll(record);
            } catch (RuntimeException exception) {
                log.warn("Credit status poll failed for applyId={}", record.applyId(), exception);
            }
        }
    }
}
