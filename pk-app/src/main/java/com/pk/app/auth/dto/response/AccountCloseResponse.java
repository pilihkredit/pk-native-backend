package com.pk.app.auth.dto.response;

/**
 * Account closure result returned to the client.
 *
 * @param closedAt     account closed time (epoch millis), maps to {@code user_profile.deleted_at}
 * @param dataDeleteAt scheduled data deletion time (epoch millis), maps to {@code user_profile.retention_until}
 */
public record AccountCloseResponse(
        long closedAt,
        long dataDeleteAt
) {
}
