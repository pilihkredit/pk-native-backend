package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.port.LenderEnumMapper;
import com.pk.core.profile.port.LenderProfileQueryPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileEnumCatalog;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ProfileProperties.class, ProfileSyncProperties.class})
@org.springframework.context.annotation.Import({
        com.pk.infra.ocr.OcrInfraConfiguration.class,
        com.pk.infra.biometric.BiometricStorageConfiguration.class
})
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
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor
    ) {
        return new ProfileSyncPayloadLoader(
                profilePersonalRepository,
                profileContactRepository,
                sensitiveFieldEncryptor
        );
    }

    @Bean
    LenderSyncAuditRequestBuilder lenderSyncAuditRequestBuilder(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            com.pk.core.profile.port.ProfileBankCardRepository profileBankCardRepository,
            com.pk.core.profile.port.ProfileLoginLogRepository profileLoginLogRepository,
            ObjectMapper objectMapper
    ) {
        return new LenderSyncAuditRequestBuilder(
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileLoginLogRepository,
                objectMapper
        );
    }

    @Bean
    ProfileQueryFacade profileQueryFacade(
            LenderProfileQueryPort lenderProfileQueryPort,
            ObjectMapper objectMapper
    ) {
        return new ProfileQueryFacade(lenderProfileQueryPort, objectMapper);
    }

    @Bean
    ProfileSyncHandler profileSyncHandler(
            LenderProfileSyncPort lenderProfileSyncPort,
            ProfileSyncPayloadLoader profileSyncPayloadLoader,
            com.pk.core.profile.port.UserProfileBindingRepository userProfileBindingRepository,
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            com.pk.core.profile.port.ProfileBankCardRepository profileBankCardRepository,
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            com.pk.core.profile.port.ProfileLoginLogRepository profileLoginLogRepository,
            com.pk.core.profile.port.ProfileAfRepository profileAfRepository,
            com.pk.core.profile.port.ProfileTongdunRepository profileTongdunRepository
    ) {
        return new ProfileSyncHandler(
                lenderProfileSyncPort,
                profileSyncPayloadLoader,
                userProfileBindingRepository,
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileIdentityRepository,
                profileLoginLogRepository,
                profileAfRepository,
                profileTongdunRepository
        );
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
            LenderProfileQueryPort lenderProfileQueryPort,
            ObjectMapper objectMapper
    ) {
        return new OnboardingProgressFacade(lenderProfileQueryPort, objectMapper);
    }

    @Bean
    IdentityVerificationCompletionService identityVerificationCompletionService(
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            com.pk.core.profile.port.ProfileAfRepository profileAfRepository,
            UserDeviceWriter userDeviceWriter,
            com.pk.core.credit.port.ProfileVersionRepository profileVersionRepository,
            com.pk.core.profile.port.UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            ObjectMapper objectMapper
    ) {
        return new IdentityVerificationCompletionService(
                profileIdentityRepository,
                biometricImageStore,
                profileSyncOrchestrator,
                profileAfRepository,
                userDeviceWriter,
                profileVersionRepository,
                userProfileBindingRepository,
                onboardingProgressFacade,
                objectMapper
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
    IdentityOcrFacade identityOcrFacade(
            com.pk.core.profile.port.AdvanceAiOcrPort advanceAiOcrPort,
            com.pk.core.profile.port.OcrSessionStore ocrSessionStore,
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            com.pk.core.profile.port.ProfileAfRepository profileAfRepository,
            UserDeviceWriter userDeviceWriter,
            com.pk.core.credit.port.ProfileVersionRepository profileVersionRepository,
            com.pk.core.profile.port.UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            IdentityVerificationCompletionService completionService,
            com.pk.infra.ocr.OcrProperties ocrProperties,
            ObjectMapper objectMapper
    ) {
        return new IdentityOcrFacade(
                advanceAiOcrPort,
                ocrSessionStore,
                profileIdentityRepository,
                sensitiveFieldEncryptor,
                biometricImageStore,
                profileSyncOrchestrator,
                profileAfRepository,
                userDeviceWriter,
                profileVersionRepository,
                userProfileBindingRepository,
                onboardingProgressFacade,
                completionService,
                ocrProperties,
                objectMapper
        );
    }

    @Bean
    BankCardListAccessFacade bankCardListAccessFacade(
            ProfileQueryFacade profileQueryFacade,
            com.pk.core.home.port.LenderUserStatusPort lenderUserStatusPort
    ) {
        return new BankCardListAccessFacade(profileQueryFacade, lenderUserStatusPort);
    }

    @Bean
    BankCardMaxConfigLoader bankCardMaxConfigLoader(
            com.pk.core.appconfig.port.AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        return new BankCardMaxConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    ProfileServiceFacade profileServiceFacade(
            com.pk.core.profile.port.ProfilePersonalRepository profilePersonalRepository,
            com.pk.core.profile.port.ProfileContactRepository profileContactRepository,
            com.pk.core.profile.port.ProfileBankCardRepository profileBankCardRepository,
            com.pk.core.profile.port.ProfileLoginLogRepository profileLoginLogRepository,
            com.pk.core.profile.port.ProfileAfRepository profileAfRepository,
            com.pk.core.profile.port.ProfileTongdunRepository profileTongdunRepository,
            UserDeviceWriter userDeviceWriter,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            ProfileEnumValidator profileEnumValidator,
            com.pk.infra.reference.BankReferenceFacade bankReferenceFacade,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            OnboardingProgressFacade onboardingProgressFacade,
            com.pk.core.profile.port.UserProfileBindingRepository userProfileBindingRepository,
            ProfileQueryFacade profileQueryFacade,
            com.pk.core.profile.port.LenderBankCardPort lenderBankCardPort,
            BankCardMaxConfigLoader bankCardMaxConfigLoader
    ) {
        return new ProfileServiceFacade(
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileLoginLogRepository,
                profileAfRepository,
                profileTongdunRepository,
                userDeviceWriter,
                sensitiveFieldEncryptor,
                profileEnumValidator,
                bankReferenceFacade,
                profileSyncOrchestrator,
                onboardingProgressFacade,
                userProfileBindingRepository,
                profileQueryFacade,
                lenderBankCardPort,
                bankCardMaxConfigLoader
        );
    }
}
