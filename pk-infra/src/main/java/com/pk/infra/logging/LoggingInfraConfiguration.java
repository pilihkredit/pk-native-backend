package com.pk.infra.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SlsLoggingProperties.class)
public class LoggingInfraConfiguration {
}
