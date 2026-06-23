package com.pk.app.platform.dto;

import java.time.Instant;

/**
 * Platform liveness snapshot.
 *
 * @param status service status, e.g. UP
 * @param time   server timestamp (UTC)
 */
public record PlatformStatus(String status, Instant time) {
}
