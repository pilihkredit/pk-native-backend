package com.pk.app.profile.dto.response;

/**
 * Response body for bank card soft-delete.
 */
public record ProfileBankCardDeleteResponse(
        String requestId,
        boolean deleted
) {
}
