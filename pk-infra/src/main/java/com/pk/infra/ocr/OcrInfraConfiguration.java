package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.infra.auth.AuthInfraConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Import(AuthInfraConfiguration.class)
@EnableConfigurationProperties(OcrProperties.class)
public class OcrInfraConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
    AdvanceAiOcrPort advanceAiOcrPort(
            OcrProperties ocrProperties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        return new AdvanceAiOcrClient(ocrProperties, redisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
    OcrSessionStore ocrSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrProperties ocrProperties
    ) {
        return new RedisOcrSessionStore(redisTemplate, objectMapper, ocrProperties);
    }
}
