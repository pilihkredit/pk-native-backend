package com.pk.infra.biometric;

import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BiometricStorageProperties.class)
public class BiometricStorageConfiguration {
    @Bean
    BiometricImageStore biometricImageStore(
            BiometricStorageProperties properties,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            @Value("${pk.environment:local}") String environment
    ) {
        if (properties.oss().enabled()) {
            return new OssBiometricImageStore(properties, sensitiveFieldEncryptor, environment);
        }
        return new LocalEncryptedBiometricImageStore(properties, sensitiveFieldEncryptor, environment);
    }
}
