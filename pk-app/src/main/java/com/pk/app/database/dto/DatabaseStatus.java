package com.pk.app.database.dto;

/**
 * Database connectivity check result.
 *
 * @param status                 database reachability status, e.g. UP
 * @param databaseProductName    JDBC database product name
 * @param databaseProductVersion JDBC database product version
 * @param validationValue        result of SELECT 1 validation query
 */
public record DatabaseStatus(
        String status,
        String databaseProductName,
        String databaseProductVersion,
        int validationValue
) {
}
