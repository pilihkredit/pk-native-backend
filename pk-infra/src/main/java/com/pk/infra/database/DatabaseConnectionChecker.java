package com.pk.infra.database;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionChecker {
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionChecker(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabaseConnectionResult check() {
        Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return new DatabaseConnectionResult(
                databaseProductName(),
                databaseProductVersion(),
                value == null ? 0 : value
        );
    }

    private String databaseProductName() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read database product name", ex);
        }
    }

    private String databaseProductVersion() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductVersion();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read database product version", ex);
        }
    }
}
