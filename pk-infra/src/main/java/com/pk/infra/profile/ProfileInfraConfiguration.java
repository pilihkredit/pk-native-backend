package com.pk.infra.profile;

import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.profile.port.LenderEnumMapper;
import com.pk.core.profile.port.ProfileEnumCatalog;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProfileProperties.class)
public class ProfileInfraConfiguration {
    @Bean
    ProfileEnumCatalog profileEnumCatalog() {
        return new PendanaanProfileEnumCatalog();
    }

    @Bean
    LenderEnumMapper lenderEnumMapper(ProfileEnumCatalog profileEnumCatalog) {
        return new PendanaanLenderEnumMapper(profileEnumCatalog);
    }

    @Bean
    ProfileEnumValidator profileEnumValidator(ProfileEnumCatalog profileEnumCatalog) {
        return new ProfileEnumValidator(profileEnumCatalog);
    }

    @Bean
    AreaHierarchyValidator areaHierarchyValidator() {
        return new BasicAreaHierarchyValidator();
    }

    @Bean
    SensitiveFieldEncryptor sensitiveFieldEncryptor(ProfileProperties profileProperties) {
        return new AesGcmSensitiveFieldEncryptor(profileProperties.fieldEncryptionKey());
    }

    @Bean
    ProfileServiceFacade profileServiceFacade(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            AreaHierarchyValidator areaHierarchyValidator,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator
    ) {
        return new ProfileServiceFacade(
                profilePersonalRepository,
                areaHierarchyValidator,
                sensitiveFieldEncryptor,
                profileEnumValidator
        );
    }
}
