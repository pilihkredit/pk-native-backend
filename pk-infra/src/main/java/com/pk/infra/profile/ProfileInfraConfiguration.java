package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.AreaHierarchyValidator;
import com.pk.core.profile.port.LenderEnumMapper;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileEnumCatalog;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ProfileProperties.class, ProfileSyncProperties.class})
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
    SensitiveFieldEncryptor sensitiveFieldEncryptor(ProfileProperties profileProperties) {
        return new AesGcmSensitiveFieldEncryptor(profileProperties.fieldEncryptionKey());
    }

    @Bean
    ProfileSyncPayloadLoader profileSyncPayloadLoader(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileWorkRepository profileWorkRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor
    ) {
        return new ProfileSyncPayloadLoader(
                profilePersonalRepository,
                profileWorkRepository,
                profileContactRepository,
                sensitiveFieldEncryptor
        );
    }

    @Bean
    ProfileSyncHandler profileSyncHandler(
            LenderProfileSyncPort lenderProfileSyncPort,
            ProfileSyncPayloadLoader profileSyncPayloadLoader
    ) {
        return new ProfileSyncHandler(lenderProfileSyncPort, profileSyncPayloadLoader);
    }

    @Bean
    ProfileSyncOutboxPublisher profileSyncOutboxPublisher(
            com.pk.core.outbox.port.OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        return new ProfileSyncOutboxPublisher(outboxEventRepository, objectMapper);
    }

    @Bean
    ProfileSyncOrchestrator profileSyncOrchestrator(ProfileSyncHandler profileSyncHandler) {
        return new ProfileSyncOrchestrator(profileSyncHandler);
    }

    @Bean
    OnboardingProgressFacade onboardingProgressFacade(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileWorkRepository profileWorkRepository,
            com.pk.core.profile.port.ProfileBankCardRepository profileBankCardRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            com.pk.core.profile.port.ProfileDeviceRepository profileDeviceRepository
    ) {
        return new OnboardingProgressFacade(
                profilePersonalRepository,
                profileWorkRepository,
                profileBankCardRepository,
                profileContactRepository,
                profileDeviceRepository
        );
    }

    @Bean
    ProfileServiceFacade profileServiceFacade(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileWorkRepository profileWorkRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            com.pk.core.profile.port.ProfileBankCardRepository profileBankCardRepository,
            com.pk.core.profile.port.ProfileDeviceRepository profileDeviceRepository,
            AreaHierarchyValidator areaHierarchyValidator,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator,
            com.pk.infra.reference.BankReferenceFacade bankReferenceFacade,
            ProfileSyncOrchestrator profileSyncOrchestrator
    ) {
        return new ProfileServiceFacade(
                profilePersonalRepository,
                profileWorkRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileDeviceRepository,
                areaHierarchyValidator,
                sensitiveFieldEncryptor,
                profileEnumValidator,
                bankReferenceFacade,
                profileSyncOrchestrator
        );
    }
}
