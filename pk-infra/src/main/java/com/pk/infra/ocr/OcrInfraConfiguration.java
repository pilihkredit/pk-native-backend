package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.core.profile.port.TrustDecisionSessionStore;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.infra.auth.AuthInfraConfiguration;
import com.pk.infra.ocr.mapper.OcrVendorCallLogMapper;
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
    OcrProviderConfigLoader ocrProviderConfigLoader(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        return new OcrProviderConfigLoader(appConfigRepository, objectMapper);
    }

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
    AdvanceAiOcrPort advanceAiOcrPort(
            OcrProviderConfigLoader configLoader,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter ocrVendorCallLogWriter,
            OcrSensitiveJsonSupport ocrSensitiveJsonSupport
    ) {
        return new AppConfigAdvanceAiOcrClient(
                configLoader,
                redisTemplate,
                objectMapper,
                ocrVendorCallLogWriter,
                ocrSensitiveJsonSupport
        );
    }

    @Bean
    OcrSessionStore ocrSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrProviderConfigLoader configLoader
    ) {
        return new RedisOcrSessionStore(redisTemplate, objectMapper, configLoader);
    }

    @Bean
    TrustDecisionKycPort trustDecisionKycPort(
            OcrProviderConfigLoader configLoader,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter ocrVendorCallLogWriter,
            OcrSensitiveJsonSupport ocrSensitiveJsonSupport
    ) {
        return new AppConfigTrustDecisionKycClient(
                configLoader, objectMapper, ocrVendorCallLogWriter, ocrSensitiveJsonSupport);
    }

    @Bean
    TrustDecisionSessionStore trustDecisionSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrProviderConfigLoader configLoader
    ) {
        return new RedisTrustDecisionSessionStore(redisTemplate, objectMapper, configLoader);
    }
}
