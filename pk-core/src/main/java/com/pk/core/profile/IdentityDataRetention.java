package com.pk.core.profile;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;

/**
 * Compliance retention for identity data after account closure.
 */
public final class IdentityDataRetention {
  public static final Period RETENTION_PERIOD = Period.ofYears(5);

  private IdentityDataRetention() {
  }

  public static Instant retentionUntil(Instant accountClosedAt) {
    return accountClosedAt.atZone(ZoneOffset.UTC).plus(RETENTION_PERIOD).toInstant();
  }
}
