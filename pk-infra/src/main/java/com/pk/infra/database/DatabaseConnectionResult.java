package com.pk.infra.database;

public record DatabaseConnectionResult(
        String databaseProductName,
        String databaseProductVersion,
        int validationValue
) {
}
