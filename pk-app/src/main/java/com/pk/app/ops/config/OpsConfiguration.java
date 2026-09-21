package com.pk.app.ops.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpsBackofficeProperties.class)
public class OpsConfiguration {
}
