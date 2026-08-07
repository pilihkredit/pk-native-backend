package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditStatusBackfillService {
    private static final Logger log = LoggerFactory.getLogger(CreditStatusBackfillService.class);
    private static final int DEFAULT_BATCH_SIZE = 50;

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusPollHandler creditStatusPollHandler;

    public CreditStatusBackfillService(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusPollHandler creditStatusPollHandler
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusPollHandler = creditStatusPollHandler;
    }

    public BackfillResult run() {
        return run(DEFAULT_BATCH_SIZE);
    }

    public BackfillResult run(int batchSize) {
        int limit = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        List<CreditApplicationRepository.CreditApplicationRecord> due =
                creditApplicationRepository.findDueForStatusBackfill(limit);
        int success = 0;
        int failed = 0;
        for (CreditApplicationRepository.CreditApplicationRecord record : due) {
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
        }
        return new BackfillResult(due.size(), success, failed);
    }

    public record BackfillResult(int candidates, int success, int failed) {
    }
}
