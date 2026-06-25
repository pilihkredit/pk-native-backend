package com.pk.app.profile.dto.response;

/**
 * Response body for bank card submission.
 */
public record ProfileBankCardSaveResponse(
        String requestId,
        String verifyStatus,
        String cardNoMasked
) {
}
