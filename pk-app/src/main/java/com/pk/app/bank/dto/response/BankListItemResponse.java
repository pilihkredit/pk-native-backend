package com.pk.app.bank.dto.response;

public record BankListItemResponse(
        String bankCode,
        String bankName,
        String bankType,
        String iconUrl
) {
}
