package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.core.profile.port.TrustDecisionSessionStore;
import com.pk.infra.auth.AuthInfraConfiguration;
import com.pk.infra.ocr.mapper.OcrVendorCallLogMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Import(AuthInfraConfiguration.class)
@EnableConfigurationProperties({OcrProperties.class, TrustDecisionProperties.class})
public class OcrInfraConfiguration {
    @Bean
    OcrVendorCallLogWriter ocrVendorCallLogWriter(OcrVendorCallLogMapper mapper) {
        return new OcrVendorCallLogWriterImpl(mapper);
    }

    @Bean
    OcrSensitiveJsonSupport ocrSensitiveJsonSupport(
            ObjectMapper objectMapper,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            BiometricImageStore biometricImageStore
    ) {
        return new OcrSensitiveJsonSupport(objectMapper, sensitiveFieldEncryptor, biometricImageStore);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
    AdvanceAiOcrPort advanceAiOcrPort(
            OcrProperties ocrProperties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter ocrVendorCallLogWriter,
            OcrSensitiveJsonSupport ocrSensitiveJsonSupport
    ) {
        return new AdvanceAiOcrClient(
                ocrProperties,
                redisTemplate,
                objectMapper,
                ocrVendorCallLogWriter,
                ocrSensitiveJsonSupport
        );
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

    @Bean
    @ConditionalOnProperty(prefix = "pk.trustdecision", name = "enabled", havingValue = "true")
    TrustDecisionKycPort trustDecisionKycPort(
            TrustDecisionProperties properties,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter ocrVendorCallLogWriter,
            OcrSensitiveJsonSupport ocrSensitiveJsonSupport
    ) {
        properties.validateEnabledSettings();
        return new TrustDecisionKycClient(
                properties, objectMapper, ocrVendorCallLogWriter, ocrSensitiveJsonSupport);
    }

    @Bean
    @ConditionalOnProperty(prefix = "pk.trustdecision", name = "enabled", havingValue = "true")
    TrustDecisionSessionStore trustDecisionSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            TrustDecisionProperties properties
    ) {
        return new RedisTrustDecisionSessionStore(redisTemplate, objectMapper, properties);
    }
}
