package com.pk.infra.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.review.ReviewSandboxConfigPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewSandboxInfraConfiguration {
    @Bean
    ReviewSandboxConfigPort reviewSandboxConfigPort(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        return new AppConfigReviewSandboxConfigPort(appConfigRepository, objectMapper);
    }
}
