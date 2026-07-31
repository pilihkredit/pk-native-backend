package com.pk.app.profile.dto.response;

/**
 * Response for bank card list access gate.
 */
public record ProfileBankCardListAccessResponse(
        boolean canShowList
) {
}
