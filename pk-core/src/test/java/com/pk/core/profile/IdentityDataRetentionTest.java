package com.pk.core.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class IdentityDataRetentionTest {
    @Test
    void retentionUntilAddsFiveYearsFromAccountClosure() {
        Instant closedAt = Instant.parse("2026-01-15T10:30:00Z");
        Instant retentionUntil = IdentityDataRetention.retentionUntil(closedAt);
        assertThat(retentionUntil).isEqualTo(
                closedAt.atZone(ZoneOffset.UTC).plus(Period.ofYears(5)).toInstant()
        );
    }
}
