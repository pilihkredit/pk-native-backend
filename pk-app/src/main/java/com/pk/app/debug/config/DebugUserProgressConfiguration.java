package com.pk.app.debug.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DebugUserProgressProperties.class)
public class DebugUserProgressConfiguration {
}
