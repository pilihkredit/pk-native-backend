package com.pk.app.common.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApiLoggingProperties.class)
public class ApiLoggingConfiguration {
}
