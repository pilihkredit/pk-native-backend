package com.pk.app.profile.dto.response;

/**
 * Result of saving manual identity basic info (local only, not synced to lender yet).
 */
public record IdentityBasicSaveResponse(
        String requestId,
        String moduleStatus
) {
}
