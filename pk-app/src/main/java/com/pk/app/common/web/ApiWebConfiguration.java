package com.pk.app.common.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiWebConfiguration implements WebMvcConfigurer {
    private final ApiProperties apiProperties;

    public ApiWebConfiguration(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        String prefix = apiProperties.v1Prefix();
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        String normalized = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        configurer.addPathPrefix(
                normalized,
                handlerType -> handlerType.isAnnotationPresent(RestController.class)
                        && handlerType.getPackageName().startsWith("com.pk.app")
        );
    }
}
