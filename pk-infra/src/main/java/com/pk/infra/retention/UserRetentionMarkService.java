package com.pk.infra.retention;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Job A: scan user_deleted(pending) and set user_profile.retention_until.
 */
public class UserRetentionMarkService {
    private static final Logger log = LoggerFactory.getLogger(UserRetentionMarkService.class);
    private static final int FALLBACK_BATCH_SIZE = 50;
    private static final int FALLBACK_YEARS = 5;
    private static final String FALLBACK_ZONE_ID = "Asia/Jakarta";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserRetentionJdbcRepository repository;
    private final UserRetentionProperties properties;
    private final TransactionTemplate transactionTemplate;

    public UserRetentionMarkService(
            UserRetentionJdbcRepository repository,
            UserRetentionProperties properties,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.properties = properties;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public MarkResult run() {
        return run(configuredBatchSize(), null, null);
    }

    /**
     * @param batchSize page size
     * @param retentionUntilDate optional absolute date (keep until end of that day)
     * @param retentionYears optional years from now; ignored if retentionUntilDate set
     */
    public MarkResult run(int batchSize, LocalDate retentionUntilDate, Integer retentionYears) {
        int pageSize = batchSize > 0 ? batchSize : configuredBatchSize();
        ZoneId zone = resolveZoneId();
        LocalDateTime retentionUntil = resolveRetentionUntil(zone, retentionUntilDate, retentionYears);
        LocalDateTime markedAt = LocalDateTime.now(zone);

        long afterId = 0L;
        int candidates = 0;
        int success = 0;
        int failed = 0;
        int pages = 0;

        log.info(
                "User retention mark start pageSize={} retentionUntil={} zone={}",
                pageSize,
                retentionUntil,
                zone
        );

        while (true) {
            List<UserRetentionJdbcRepository.UserDeletedRow> rows =
                    repository.findPendingDeleted(afterId, pageSize);
            if (rows.isEmpty()) {
                break;
            }
            pages++;
            for (UserRetentionJdbcRepository.UserDeletedRow row : rows) {
                candidates++;
                try {
                    transactionTemplate.executeWithoutResult(status -> markOne(row, retentionUntil, markedAt));
                    success++;
                } catch (RuntimeException exception) {
                    failed++;
                    log.warn("User retention mark failed id={} userId={}", row.id(), row.userId(), exception);
                    try {
                        repository.markDeletedFailed(row.id(), exception.getMessage());
                    } catch (RuntimeException updateEx) {
                        log.warn("Failed to write mark failure status id={}", row.id(), updateEx);
                    }
                }
                afterId = row.id();
            }
            if (rows.size() < pageSize) {
                break;
            }
        }

        return new MarkResult(candidates, success, failed, pages, pageSize, retentionUntil);
    }

    private void markOne(
            UserRetentionJdbcRepository.UserDeletedRow row,
            LocalDateTime retentionUntil,
            LocalDateTime markedAt
    ) {
        if (!repository.userProfileExists(row.userId())) {
            throw new IllegalStateException("user_profile not found for userId=" + row.userId());
        }
        int updated = repository.updateProfileRetentionUntil(row.userId(), retentionUntil);
        if (updated != 1) {
            throw new IllegalStateException("failed to update retention_until for userId=" + row.userId());
        }
        int marked = repository.markDeletedRow(row.id(), retentionUntil, markedAt);
        if (marked != 1) {
            throw new IllegalStateException("failed to mark user_deleted id=" + row.id());
        }
    }

    public int configuredBatchSize() {
        int configured = properties == null ? FALLBACK_BATCH_SIZE : properties.batchSize();
        return configured > 0 ? configured : FALLBACK_BATCH_SIZE;
    }

    public int configuredDefaultYears() {
        int configured = properties == null ? FALLBACK_YEARS : properties.defaultYears();
        return configured > 0 ? configured : FALLBACK_YEARS;
    }

    public ZoneId resolveZoneId() {
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

    LocalDateTime resolveRetentionUntil(ZoneId zone, LocalDate retentionUntilDate, Integer retentionYears) {
        LocalDate day;
        if (retentionUntilDate != null) {
            day = retentionUntilDate;
        } else {
            int years = retentionYears != null && retentionYears > 0
                    ? retentionYears
                    : configuredDefaultYears();
            day = LocalDate.now(zone).plusYears(years);
        }
        return LocalDateTime.of(day, LocalTime.of(23, 59, 59, 999_000_000));
    }

    public static LocalDate parseRetentionUntilDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid retentionUntil date, expect yyyy-MM-dd: " + raw, ex);
        }
    }

    public record MarkResult(
            int candidates,
            int success,
            int failed,
            int pages,
            int pageSize,
            LocalDateTime retentionUntil
    ) {
    }
}
