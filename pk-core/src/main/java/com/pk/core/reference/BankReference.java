package com.pk.core.reference;

public record BankReference(
        String bankCode,
        String bankName,
        Integer bankType,
        String iconUrl
) {
}
