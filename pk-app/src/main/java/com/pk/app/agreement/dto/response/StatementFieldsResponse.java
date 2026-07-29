package com.pk.app.agreement.dto.response;

public record StatementFieldsResponse(
        String borrowerName,
        String borrowerNik,
        String effectiveDateDisplay
) {
}
