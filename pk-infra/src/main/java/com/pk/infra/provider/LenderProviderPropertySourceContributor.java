package com.pk.infra.provider;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

final class LenderProviderPropertySourceContributor {
    static final String PROPERTY_SOURCE_NAME = "lenderProviderDatabase";
    static final String CONFIG_SOURCE_KEY = "pk.lender.config.source";
    static final String PROVIDER_CODE_KEY = "pk.lender.config.provider-code";

    private static final Logger log = LoggerFactory.getLogger(LenderProviderPropertySourceContributor.class);

    private LenderProviderPropertySourceContributor() {
    }

    static boolean contribute(ConfigurableEnvironment environment) {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return true;
        }
        String source = environment.getProperty(CONFIG_SOURCE_KEY, "db");
        if ("env".equalsIgnoreCase(source)) {
            return false;
        }
        String providerCode = environment.getProperty(PROVIDER_CODE_KEY, LenderProviderCodes.PENDANAAN);
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            log.warn(
                    "pk.lender.config.source=db but spring.datasource.url is missing; "
                            + "lender credentials will not be loaded from database"
            );
            return false;
        }
        Optional<Map<String, Object>> properties = LenderProviderConfigReader.loadSpringProperties(
                jdbcUrl,
                username,
                password,
                providerCode
        );
        if (properties.isEmpty()) {
            log.warn(
                    "No active lender provider config found in database for providerCode={}; "
                            + "apply sql/create_pk_schema.sql",
                    providerCode
            );
            return false;
        }
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties.get()));
        log.info("Loaded lender provider config from database for providerCode={}", providerCode);
        return true;
    }
}
