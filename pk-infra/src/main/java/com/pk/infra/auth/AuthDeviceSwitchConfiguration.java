package com.pk.infra.auth;

import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import com.pk.infra.profile.TrustDecisionIdentityFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthDeviceSwitchConfiguration {
    @Bean
    DeviceSwitchSecurityConfigLoader deviceSwitchSecurityConfigLoader(
            com.pk.core.appconfig.port.AppConfigRepository appConfigRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper
    ) {
        return new DeviceSwitchSecurityConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    DeviceSwitchLoginFaceAttemptLimiter deviceSwitchLoginFaceAttemptLimiter(
            StringRedisTemplate redisTemplate,
            DeviceSwitchSecurityConfigLoader deviceSwitchSecurityConfigLoader
    ) {
        return new DeviceSwitchLoginFaceAttemptLimiter(redisTemplate, deviceSwitchSecurityConfigLoader);
    }

    @Bean
    DeviceSwitchLivenessLicenseRateLimiter deviceSwitchLivenessLicenseRateLimiter(
            StringRedisTemplate redisTemplate,
            DeviceSwitchSecurityConfigLoader deviceSwitchSecurityConfigLoader
    ) {
        return new DeviceSwitchLivenessLicenseRateLimiter(redisTemplate, deviceSwitchSecurityConfigLoader);
    }

    @Bean
    DeviceSwitchLivenessLicenseFacade deviceSwitchLivenessLicenseFacade(
            UserAuthRepository userAuthRepository,
            LoginDeviceSwitchGateService loginDeviceSwitchGateService,
            TrustDecisionIdentityFacade trustDecisionIdentityFacade,
            DeviceSwitchLivenessLicenseRateLimiter deviceSwitchLivenessLicenseRateLimiter,
            DeviceSwitchSecurityConfigLoader deviceSwitchSecurityConfigLoader
    ) {
        return new DeviceSwitchLivenessLicenseFacade(
                userAuthRepository,
                loginDeviceSwitchGateService,
                trustDecisionIdentityFacade,
                deviceSwitchLivenessLicenseRateLimiter,
                deviceSwitchSecurityConfigLoader
        );
    }

    @Bean
    LoginDeviceSwitchGateService loginDeviceSwitchGateService(
            UserAuthRepository userAuthRepository,
            com.pk.infra.profile.FaceComparisonBaselineResolver faceComparisonBaselineResolver,
            DeviceSwitchFaceVerificationRepository deviceSwitchFaceVerificationRepository
    ) {
        return new LoginDeviceSwitchGateService(
                userAuthRepository,
                faceComparisonBaselineResolver,
                deviceSwitchFaceVerificationRepository
        );
    }

    @Bean
    DeviceSwitchLoginFaceFacade deviceSwitchLoginFaceFacade(
            FaceVerifyTicketTtlConfigLoader faceVerifyTicketTtlConfigLoader,
            UserAuthRepository userAuthRepository,
            TrustDecisionKycPort trustDecisionKycPort,
            AdvanceAiOcrPort advanceAiOcrPort,
            com.pk.infra.profile.FaceComparisonBaselineResolver faceComparisonBaselineResolver,
            DeviceSwitchFaceVerificationRepository deviceSwitchFaceVerificationRepository,
            BiometricImageStore biometricImageStore,
            OcrProviderConfigLoader configLoader,
            DeviceSwitchLoginFaceAttemptLimiter deviceSwitchLoginFaceAttemptLimiter
    ) {
        return new DeviceSwitchLoginFaceFacade(
                faceVerifyTicketTtlConfigLoader,
                userAuthRepository,
                trustDecisionKycPort,
                advanceAiOcrPort,
                faceComparisonBaselineResolver,
                deviceSwitchFaceVerificationRepository,
                biometricImageStore,
                configLoader,
                deviceSwitchLoginFaceAttemptLimiter
        );
    }
}
