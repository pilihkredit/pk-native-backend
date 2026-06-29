package com.pk.infra.provider;

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class LenderProviderEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    static final String PROPERTY_SOURCE_NAME = "lenderProviderDatabase";
    static final String CONFIG_SOURCE_KEY = "pk.lender.config.source";
    static final String PROVIDER_CODE_KEY = "pk.lender.config.provider-code";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String source = environment.getProperty(CONFIG_SOURCE_KEY, "db");
        if ("env".equalsIgnoreCase(source)) {
            return;
        }
        String providerCode = environment.getProperty(PROVIDER_CODE_KEY, LenderProviderCodes.PENDANAAN);
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        Optional<Map<String, Object>> properties = LenderProviderConfigReader.loadSpringProperties(
                jdbcUrl,
                username,
                password,
                providerCode
        );
        properties.ifPresent(values -> environment.getPropertySources().addFirst(
                new MapPropertySource(PROPERTY_SOURCE_NAME, values)
        ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
