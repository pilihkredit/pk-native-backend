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
@EnableConfigurationProperties({
        ProfileProperties.class,
        ProfileSyncProperties.class,
        ProfileSyncLockProperties.class
})
@org.springframework.context.annotation.Import({
        com.pk.infra.ocr.OcrInfraConfiguration.class,
        com.pk.infra.biometric.BiometricStorageConfiguration.class
})
public class ProfileInfraConfiguration {
    @Bean
    ProfileEnumCatalog profileEnumCatalog() {
        return new ApiPartnerProfileEnumCatalog();
    }

    @Bean
    LenderEnumMapper lenderEnumMapper(ProfileEnumCatalog profileEnumCatalog) {
        return new ApiPartnerLenderEnumMapper(profileEnumCatalog);
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
    RedisProfileSyncUserLock redisProfileSyncUserLock(
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            ProfileSyncLockProperties properties
    ) {
        return new RedisProfileSyncUserLock(redisTemplate, properties);
    }

    @Bean
    OnboardingProgressFacade onboardingProgressFacade(
            LenderProfileQueryPort lenderProfileQueryPort,
            ObjectMapper objectMapper
    ) {
        return new OnboardingProgressFacade(lenderProfileQueryPort, objectMapper);
    }

    @Bean
    AppsFlyerLenderPayloadResolver appsFlyerLenderPayloadResolver(
            com.pk.core.callback.port.AppsFlyerCallbackRepository appsFlyerCallbackRepository
    ) {
        return new AppsFlyerLenderPayloadResolver(appsFlyerCallbackRepository);
    }

    @Bean
    IdentityVerificationCompletionService identityVerificationCompletionService(
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
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
                userDeviceWriter,
                profileVersionRepository,
                userProfileBindingRepository,
                onboardingProgressFacade,
                objectMapper
        );
    }

    @Bean
    IdentityOcrFacade identityOcrFacade(
            com.pk.core.profile.port.AdvanceAiOcrPort advanceAiOcrPort,
            com.pk.core.profile.port.OcrSessionStore ocrSessionStore,
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            UserDeviceWriter userDeviceWriter,
            com.pk.core.credit.port.ProfileVersionRepository profileVersionRepository,
            com.pk.core.profile.port.UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            IdentityVerificationCompletionService completionService,
            com.pk.infra.ocr.OcrProviderConfigLoader configLoader,
            ObjectMapper objectMapper
    ) {
        return new IdentityOcrFacade(
                advanceAiOcrPort,
                ocrSessionStore,
                profileIdentityRepository,
                sensitiveFieldEncryptor,
                biometricImageStore,
                profileSyncOrchestrator,
                userDeviceWriter,
                profileVersionRepository,
                userProfileBindingRepository,
                onboardingProgressFacade,
                completionService,
                configLoader,
                objectMapper
        );
    }

    @Bean
    TrustDecisionIdentityFacade trustDecisionIdentityFacade(
            com.pk.core.profile.port.TrustDecisionKycPort trustDecisionKycPort,
            com.pk.core.profile.port.TrustDecisionSessionStore trustDecisionSessionStore,
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            IdentityVerificationCompletionService completionService,
            com.pk.infra.ocr.OcrProviderConfigLoader configLoader,
            ObjectMapper objectMapper
    ) {
        return new TrustDecisionIdentityFacade(
                trustDecisionKycPort,
                trustDecisionSessionStore,
                profileIdentityRepository,
                sensitiveFieldEncryptor,
                biometricImageStore,
                completionService,
                configLoader,
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
    FaceComparisonBaselineResolver faceComparisonBaselineResolver(
            com.pk.infra.profile.mapper.FaceBaselineMapper faceBaselineMapper,
            com.pk.core.profile.port.ProfileIdentityRepository profileIdentityRepository
    ) {
        return new FaceComparisonBaselineResolver(faceBaselineMapper, profileIdentityRepository);
    }

    @Bean
    BankCardAddFaceGateService bankCardAddFaceGateService(
            com.pk.core.profile.port.BankCardAddFaceVerificationRepository bankCardAddFaceVerificationRepository
    ) {
        return new BankCardAddFaceGateService(bankCardAddFaceVerificationRepository);
    }

    @Bean
    BankCardAddFaceFacade bankCardAddFaceFacade(
            com.pk.core.profile.port.TrustDecisionKycPort trustDecisionKycPort,
            com.pk.core.profile.port.AdvanceAiOcrPort advanceAiOcrPort,
            FaceComparisonBaselineResolver faceComparisonBaselineResolver,
            com.pk.core.profile.port.BankCardAddFaceVerificationRepository bankCardAddFaceVerificationRepository,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            com.pk.infra.ocr.OcrProviderConfigLoader configLoader
    ) {
        return new BankCardAddFaceFacade(
                trustDecisionKycPort,
                advanceAiOcrPort,
                faceComparisonBaselineResolver,
                bankCardAddFaceVerificationRepository,
                biometricImageStore,
                configLoader
        );
    }

    @Bean
    MobileChangeFaceFacade mobileChangeFaceFacade(
            com.pk.core.profile.port.TrustDecisionKycPort trustDecisionKycPort,
            com.pk.core.profile.port.AdvanceAiOcrPort advanceAiOcrPort,
            FaceComparisonBaselineResolver faceComparisonBaselineResolver,
            com.pk.core.profile.port.MobileChangeFaceVerificationRepository verificationRepository,
            com.pk.core.profile.port.BiometricImageStore biometricImageStore,
            com.pk.infra.ocr.OcrProviderConfigLoader configLoader
    ) {
        return new MobileChangeFaceFacade(
                trustDecisionKycPort,
                advanceAiOcrPort,
                faceComparisonBaselineResolver,
                verificationRepository,
                biometricImageStore,
                configLoader
        );
    }

    @Bean
    MobileChangeOtpFacade mobileChangeOtpFacade(
            com.pk.core.auth.port.UserAuthRepository userAuthRepository,
            com.pk.core.profile.port.MobileChangeFaceVerificationRepository verificationRepository,
            com.pk.core.auth.port.MobileChangeOtpChallengeStore challengeStore,
            com.pk.core.auth.port.SmsSendLogRepository smsSendLogRepository,
            com.pk.core.auth.port.SmsSender smsSender,
            com.pk.core.auth.port.WhatsAppSendLogRepository whatsAppSendLogRepository,
            com.pk.core.auth.port.WhatsAppSender whatsAppSender,
            com.pk.infra.auth.AuthOtpConfigLoader otpConfigLoader,
            com.pk.infra.auth.SmsConfigLoader smsConfigLoader,
            com.pk.infra.auth.WhatsAppConfigLoader whatsAppConfigLoader
    ) {
        return new MobileChangeOtpFacade(
                userAuthRepository,
                verificationRepository,
                challengeStore,
                smsSendLogRepository,
                smsSender,
                whatsAppSendLogRepository,
                whatsAppSender,
                otpConfigLoader,
                smsConfigLoader,
                whatsAppConfigLoader
        );
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
            BankCardMaxConfigLoader bankCardMaxConfigLoader,
            com.pk.core.callback.port.AppsFlyerCallbackRepository appsFlyerCallbackRepository,
            RedisProfileSyncUserLock profileSyncUserLock,
            BankCardAddFaceGateService bankCardAddFaceGateService
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
                bankCardMaxConfigLoader,
                appsFlyerCallbackRepository,
                profileSyncUserLock,
                bankCardAddFaceGateService
        );
    }
}
