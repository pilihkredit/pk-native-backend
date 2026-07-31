package com.pk.infra.biometric;

import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BiometricStorageProperties.class)
public class BiometricStorageConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BiometricStorageConfiguration.class);

    @Bean
    @ConditionalOnProperty(prefix = "pk.biometric-storage.oss", name = "enabled", havingValue = "true")
    BiometricImageStore ossBiometricImageStore(
            BiometricStorageProperties properties,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            @Value("${pk.environment:local}") String environment
    ) {
        log.info(
                "Biometric image storage backend: OSS (bucket={}, endpoint={}, environment={})",
                properties.oss().bucket(),
                properties.oss().endpoint(),
                environment
        );
        return new OssBiometricImageStore(properties, sensitiveFieldEncryptor, environment);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "pk.biometric-storage.oss",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    BiometricImageStore localBiometricImageStore(
            BiometricStorageProperties properties,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            @Value("${pk.environment:local}") String environment
    ) {
        log.info(
                "Biometric image storage backend: local (baseDir={}, environment={})",
                properties.local().baseDir(),
                environment
        );
        return new LocalEncryptedBiometricImageStore(properties, sensitiveFieldEncryptor, environment);
    }
}
