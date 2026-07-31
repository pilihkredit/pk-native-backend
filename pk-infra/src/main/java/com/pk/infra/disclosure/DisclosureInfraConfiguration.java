package com.pk.infra.disclosure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DisclosureProperties.class)
public class DisclosureInfraConfiguration {
    @Bean
    DisclosureFacade disclosureFacade(DisclosureProperties disclosureProperties) {
        return new DisclosureFacade(disclosureProperties);
    }
}
