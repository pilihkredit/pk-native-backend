package com.pk.app.profile.dto.response;

/**
 * Personal module save result.
 *
 * @param requestId    echoed idempotency key
 * @param moduleStatus onboarding module status; always COMPLETED on success
 */
public record ProfilePersonalSaveResponse(String requestId, String moduleStatus) {
}
