package com.pk.worker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WorkerReferenceProperties.class, WorkerOutboxProperties.class})
public class WorkerConfiguration {
}
