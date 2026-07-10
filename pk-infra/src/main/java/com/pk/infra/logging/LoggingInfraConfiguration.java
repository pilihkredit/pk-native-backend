package com.pk.infra.logging;

import com.pk.core.logging.PlatformStructuredLogger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SlsLoggingProperties.class)
public class LoggingInfraConfiguration {
    @Bean
    PlatformStructuredLogger platformStructuredLogger(StructuredLogWriter structuredLogWriter) {
        return structuredLogWriter;
    }
}
