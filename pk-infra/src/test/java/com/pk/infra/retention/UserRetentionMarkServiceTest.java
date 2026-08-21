package com.pk.infra.retention;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

class UserRetentionMarkServiceTest {
    @Test
    void resolveRetentionUntilUsesDefaultYearsEndOfDay() {
        UserRetentionProperties properties = new UserRetentionProperties();
        properties.setDefaultYears(5);
        properties.setZoneId("Asia/Jakarta");
        UserRetentionMarkService service = new UserRetentionMarkService(
                null,
                properties,
                noopTxManager()
        );

        ZoneId zone = ZoneId.of("Asia/Jakarta");
        LocalDateTime actual = service.resolveRetentionUntil(zone, null, null);
        LocalDate expectedDay = LocalDate.now(zone).plusYears(5);
        assertThat(actual).isEqualTo(LocalDateTime.of(expectedDay, LocalTime.of(23, 59, 59, 999_000_000)));
    }

    @Test
    void resolveRetentionUntilPrefersAbsoluteDate() {
        UserRetentionMarkService service = new UserRetentionMarkService(
                null,
                new UserRetentionProperties(),
                noopTxManager()
        );
        LocalDateTime actual = service.resolveRetentionUntil(
                ZoneId.of("Asia/Jakarta"),
                LocalDate.of(2030, 1, 15),
                1
        );
        assertThat(actual).isEqualTo(LocalDateTime.of(2030, 1, 15, 23, 59, 59, 999_000_000));
    }

    private static PlatformTransactionManager noopTxManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() throws TransactionException {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition)
                    throws TransactionException {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
            }
        };
    }
}
