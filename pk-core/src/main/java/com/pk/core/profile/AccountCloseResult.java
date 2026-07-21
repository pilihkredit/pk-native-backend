package com.pk.core.profile;

import java.time.Instant;

/**
 * Result of soft-closing a user account.
 *
 * @param closedAt      account closure time ({@code user_profile.deleted_at})
 * @param dataDeleteAt  scheduled data deletion time ({@code user_profile.retention_until} = closedAt + 5y)
 * @param alreadyClosed true when the account was already closed before this call
 */
public record AccountCloseResult(Instant closedAt, Instant dataDeleteAt, boolean alreadyClosed) {
}
