package com.pk.app.api;

public record DatabaseStatus(
        String status,
        String databaseProductName,
        String databaseProductVersion,
        int validationValue
) {
}
