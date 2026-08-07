package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditStatusBackfillService {
    private static final Logger log = LoggerFactory.getLogger(CreditStatusBackfillService.class);
    private static final int FALLBACK_BATCH_SIZE = 50;

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusPollHandler creditStatusPollHandler;
    private final CreditStatusBackfillProperties properties;

    public CreditStatusBackfillService(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusPollHandler creditStatusPollHandler,
            CreditStatusBackfillProperties properties
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusPollHandler = creditStatusPollHandler;
        this.properties = properties;
    }

    public BackfillResult run() {
        return run(configuredBatchSize());
    }

    /**
     * Drains all current candidates in one run using id-cursor paging.
     * Each credit application is queried at most once per run even if still non-terminal.
     * {@code batchSize} is only the page size, not a total cap.
     */
    public BackfillResult run(int batchSize) {
        int pageSize = batchSize > 0 ? batchSize : configuredBatchSize();
        long afterId = 0L;
        int candidates = 0;
        int success = 0;
        int failed = 0;
        int pages = 0;

        while (true) {
            List<CreditApplicationRepository.CreditApplicationRecord> due =
                    creditApplicationRepository.findDueForStatusBackfill(afterId, pageSize);
            if (due.isEmpty()) {
                break;
            }
            pages++;
            for (CreditApplicationRepository.CreditApplicationRecord record : due) {
                candidates++;
                try {
                    creditStatusPollHandler.syncFromLenderForJob(record);
                    success++;
                } catch (RuntimeException exception) {
                    failed++;
                    log.warn(
                            "Credit status backfill failed for applyId={} userId={}",
                            record.applyId(),
                            record.userId(),
                            exception
                    );
                }
                afterId = record.id();
            }
            if (due.size() < pageSize) {
                break;
            }
        }

        return new BackfillResult(candidates, success, failed, pages, pageSize);
    }

    public int configuredBatchSize() {
        int configured = properties == null ? FALLBACK_BATCH_SIZE : properties.batchSize();
        return configured > 0 ? configured : FALLBACK_BATCH_SIZE;
    }

    public record BackfillResult(int candidates, int success, int failed, int pages, int pageSize) {
    }
}
