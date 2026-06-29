package com.pk.infra.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LenderProviderConfigReader {
    private static final Logger log = LoggerFactory.getLogger(LenderProviderConfigReader.class);

    private static final String LOAD_SQL = """
            SELECT
                p.provider_code,
                p.base_url,
                p.callback_base_url,
                p.config_json,
                c.client_id,
                c.client_secret_ref,
                c.callback_client_id,
                c.callback_secret_ref
            FROM pk_provider p
            INNER JOIN pk_api_credential c ON c.provider_code = p.provider_code
            WHERE p.provider_code = ?
              AND p.status = 'ACTIVE'
              AND c.status = 'ACTIVE'
              AND c.effective_at <= CURRENT_TIMESTAMP(3)
              AND (c.expired_at IS NULL OR c.expired_at > CURRENT_TIMESTAMP(3))
            ORDER BY c.effective_at DESC, c.id DESC
            LIMIT 1
            """;

    private LenderProviderConfigReader() {
    }

    public static Optional<Map<String, Object>> loadSpringProperties(
            String jdbcUrl,
            String username,
            String password,
            String providerCode
    ) {
        if (jdbcUrl == null || jdbcUrl.isBlank() || providerCode == null || providerCode.isBlank()) {
            return Optional.empty();
        }
        try {
            return loadRecord(jdbcUrl, username, password, providerCode).map(LenderProviderConfigReader::toSpringProperties);
        } catch (SQLException exception) {
            log.warn(
                    "Failed to load lender provider config for providerCode={} from database: {}",
                    providerCode,
                    exception.getMessage()
            );
            return Optional.empty();
        }
    }

    static Map<String, Object> toSpringProperties(LenderProviderConfigRecord record) {
        Map<String, Object> properties = new LinkedHashMap<>();
        String mode = LenderProviderConfigJson.readText(record.configJson(), "mode")
                .orElse("http");
        properties.put("pk.lender.pendanaan.mode", mode);
        putIfPresent(properties, "pk.lender.pendanaan.base-url", record.baseUrl());
        putIfPresent(properties, "pk.lender.pendanaan.client-id", record.clientId());
        putIfPresent(properties, "pk.lender.pendanaan.client-secret", record.clientSecret());
        LenderProviderConfigJson.readText(record.configJson(), "appName", "app_name")
                .ifPresent(value -> properties.put("pk.lender.pendanaan.app-name", value));
        putIfPresent(properties, "pk.callback.oauth.client-id", record.callbackClientId());
        putIfPresent(properties, "pk.callback.oauth.client-secret", record.callbackClientSecret());
        return properties;
    }

    private static Optional<LenderProviderConfigRecord> loadRecord(
            String jdbcUrl,
            String username,
            String password,
            String providerCode
    ) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                PreparedStatement statement = connection.prepareStatement(LOAD_SQL)) {
            statement.setString(1, providerCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    log.debug("No active lender provider config found for providerCode={}", providerCode);
                    return Optional.empty();
                }
                return Optional.of(new LenderProviderConfigRecord(
                        resultSet.getString("provider_code"),
                        resultSet.getString("base_url"),
                        resultSet.getString("callback_base_url"),
                        resultSet.getString("config_json"),
                        resultSet.getString("client_id"),
                        resultSet.getString("client_secret_ref"),
                        resultSet.getString("callback_client_id"),
                        resultSet.getString("callback_secret_ref")
                ));
            }
        }
    }

    private static void putIfPresent(Map<String, Object> properties, String key, String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        }
    }
}
