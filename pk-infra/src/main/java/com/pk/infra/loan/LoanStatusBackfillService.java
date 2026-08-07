package com.pk.infra.loan;

import com.pk.core.loan.port.LoanApplicationRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanStatusBackfillService {
    private static final Logger log = LoggerFactory.getLogger(LoanStatusBackfillService.class);
    private static final int FALLBACK_BATCH_SIZE = 50;
    private static final String FALLBACK_ZONE_ID = "Asia/Jakarta";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanStatusPollHandler loanStatusPollHandler;
    private final LoanStatusBackfillProperties properties;

    public LoanStatusBackfillService(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusPollHandler loanStatusPollHandler,
            LoanStatusBackfillProperties properties
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanStatusPollHandler = loanStatusPollHandler;
        this.properties = properties;
    }

    public BackfillResult run() {
        return run(configuredBatchSize());
    }

    /**
     * Drains current non-terminal loan candidates in one run using id-cursor paging.
     * Each loan application is queried at most once per run.
     * {@code batchSize} is only the page size, not a total cap.
     */
    public BackfillResult run(int batchSize) {
        int pageSize = batchSize > 0 ? batchSize : configuredBatchSize();
        Instant createdFromInclusive = resolveCreatedFromInclusive();
        long afterId = 0L;
        int candidates = 0;
        int success = 0;
        int failed = 0;
        int pages = 0;

        log.info(
                "Loan status backfill start pageSize={} lookbackDays={} createdFromInclusive={}",
                pageSize,
                configuredLookbackDays(),
                createdFromInclusive
        );

        while (true) {
            List<LoanApplicationRepository.LoanApplicationRecord> due =
                    loanApplicationRepository.findDueForStatusBackfill(
                            afterId,
                            pageSize,
                            createdFromInclusive
                    );
            if (due.isEmpty()) {
                break;
            }
            pages++;
            for (LoanApplicationRepository.LoanApplicationRecord record : due) {
                candidates++;
                try {
                    loanStatusPollHandler.syncFromLenderForJob(record);
                    success++;
                } catch (RuntimeException exception) {
                    failed++;
                    log.warn(
                            "Loan status backfill failed for loanApplyId={} userId={}",
                            record.loanApplyId(),
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

        return new BackfillResult(candidates, success, failed, pages, pageSize, createdFromInclusive);
    }

    public int configuredBatchSize() {
        int configured = properties == null ? FALLBACK_BATCH_SIZE : properties.batchSize();
        return configured > 0 ? configured : FALLBACK_BATCH_SIZE;
    }

    public int configuredLookbackDays() {
        return properties == null ? 1 : properties.lookbackDays();
    }

    Instant resolveCreatedFromInclusive() {
        int lookbackDays = configuredLookbackDays();
        if (lookbackDays <= 0) {
            return null;
        }
        ZoneId zone = resolveZoneId();
        LocalDate startDay = LocalDate.now(zone).minusDays(lookbackDays - 1L);
        return startDay.atStartOfDay(zone).toInstant();
    }

    private ZoneId resolveZoneId() {
        String zone = properties == null || properties.zoneId() == null || properties.zoneId().isBlank()
                ? FALLBACK_ZONE_ID
                : properties.zoneId().trim();
        try {
            return ZoneId.of(zone);
        } catch (Exception ignored) {
            log.warn("Invalid zoneId={}, fallback={}", zone, FALLBACK_ZONE_ID);
            return ZoneId.of(FALLBACK_ZONE_ID);
        }
    }

    public record BackfillResult(
            int candidates,
            int success,
            int failed,
            int pages,
            int pageSize,
            Instant createdFromInclusive
    ) {
    }
}
