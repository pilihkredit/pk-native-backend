package com.pk.infra.auth;

import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.core.auth.port.WhatsAppSender;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthStoreConfiguration {
    @Bean
    SessionStore sessionStore(StringRedisTemplate redisTemplate) {
        return new RedisSessionStore(redisTemplate);
    }

    @Bean
    OtpChallengeStore otpChallengeStore(StringRedisTemplate redisTemplate) {
        return new RedisOtpChallengeStore(redisTemplate);
    }

    @Bean
    OtpChallengeStore whatsappOtpChallengeStore(StringRedisTemplate redisTemplate) {
        return new RedisOtpChallengeStore(redisTemplate, "whatsapp");
    }

    @Bean
    RefreshTokenStore refreshTokenStore(StringRedisTemplate redisTemplate) {
        return new RedisRefreshTokenStore(redisTemplate);
    }

    @Bean
    AuthOtpConfigLoader authOtpConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        return new AuthOtpConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    WhatsAppConfigLoader whatsAppConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        return new WhatsAppConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    AuthServiceFacade authServiceFacade(
            AuthProperties authProperties,
            AuthOtpConfigLoader authOtpConfigLoader,
            SessionStore sessionStore,
            @Qualifier("otpChallengeStore") OtpChallengeStore otpChallengeStore,
            @Qualifier("whatsappOtpChallengeStore") OtpChallengeStore whatsappOtpChallengeStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer,
            UserAuthRepository userAuthRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender,
            WhatsAppSendLogRepository whatsAppSendLogRepository,
            WhatsAppSender whatsAppSender,
            WhatsAppConfigLoader whatsAppConfigLoader
    ) {
        return new AuthServiceFacade(
                authProperties,
                authOtpConfigLoader,
                sessionStore,
                otpChallengeStore,
                whatsappOtpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                sensitiveFieldEncryptor,
                smsSendLogRepository,
                smsSender,
                whatsAppSendLogRepository,
                whatsAppSender,
                whatsAppConfigLoader
        );
    }
}
