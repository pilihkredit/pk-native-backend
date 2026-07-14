package com.pk.infra.auth;

import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.PasswordHasher;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserPasswordCredentialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
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
    RefreshTokenStore refreshTokenStore(StringRedisTemplate redisTemplate) {
        return new RedisRefreshTokenStore(redisTemplate);
    }

    @Bean
    AuthOtpConfigLoader authOtpConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        return new AuthOtpConfigLoader(appConfigRepository, objectMapper);
    }

    @Bean
    AuthServiceFacade authServiceFacade(
            AuthProperties authProperties,
            AuthOtpConfigLoader authOtpConfigLoader,
            SessionStore sessionStore,
            OtpChallengeStore otpChallengeStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer,
            UserAuthRepository userAuthRepository,
            UserPasswordCredentialRepository userPasswordCredentialRepository,
            PasswordHasher passwordHasher,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender
    ) {
        return new AuthServiceFacade(
                authProperties,
                authOtpConfigLoader,
                sessionStore,
                otpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                userPasswordCredentialRepository,
                passwordHasher,
                smsSendLogRepository,
                smsSender
        );
    }
}
