package com.pk.infra.appconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigInfraConfiguration {
    @Bean
    AppConfigFacade appConfigFacade(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        return new AppConfigFacade(appConfigRepository, objectMapper);
    }
}
