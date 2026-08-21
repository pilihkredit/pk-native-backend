package com.pk.infra.retention;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Job B: physically delete users whose retention_until &lt;= end of today.
 */
public class UserRetentionPurgeService {
    private static final Logger log = LoggerFactory.getLogger(UserRetentionPurgeService.class);
    private static final int FALLBACK_BATCH_SIZE = 50;
    private static final String FALLBACK_ZONE_ID = "Asia/Jakarta";

    private final UserRetentionJdbcRepository repository;
    private final UserRetentionProperties properties;
    private final TransactionTemplate transactionTemplate;

    public UserRetentionPurgeService(
            UserRetentionJdbcRepository repository,
            UserRetentionProperties properties,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public PurgeResult run() {
        return run(configuredBatchSize());
    }

    public PurgeResult run(int batchSize) {
        int pageSize = batchSize > 0 ? batchSize : configuredBatchSize();
        ZoneId zone = resolveZoneId();
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(zone), LocalTime.of(23, 59, 59, 999_000_000));

        long afterId = 0L;
        int candidates = 0;
        int success = 0;
        int failed = 0;
        int pages = 0;

        log.info(
                "User retention purge start pageSize={} endOfTodayInclusive={} zone={}",
                pageSize,
                endOfToday,
                zone
        );

        while (true) {
            List<Long> userIds = repository.findUserIdsDueForPurge(endOfToday, afterId, pageSize);
            if (userIds.isEmpty()) {
                break;
            }
            pages++;
            for (Long userId : userIds) {
                candidates++;
                try {
                    transactionTemplate.executeWithoutResult(status -> repository.purgeUserCompletely(userId));
                    success++;
                } catch (RuntimeException exception) {
                    failed++;
                    log.warn("User retention purge failed userId={}", userId, exception);
                    try {
                        repository.markPurgeFailed(userId, exception.getMessage());
                    } catch (RuntimeException updateEx) {
                        log.warn("Failed to write purge failure status userId={}", userId, updateEx);
                    }
                }
                afterId = userId;
            }
            if (userIds.size() < pageSize) {
                break;
            }
        }

        return new PurgeResult(candidates, success, failed, pages, pageSize, endOfToday);
    }

    public int configuredBatchSize() {
        int configured = properties == null ? FALLBACK_BATCH_SIZE : properties.batchSize();
        return configured > 0 ? configured : FALLBACK_BATCH_SIZE;
    }

    ZoneId resolveZoneId() {
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

    public record PurgeResult(
            int candidates,
            int success,
            int failed,
            int pages,
            int pageSize,
            LocalDateTime endOfTodayInclusive
    ) {
    }
}
