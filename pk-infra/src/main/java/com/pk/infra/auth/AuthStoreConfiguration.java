package com.pk.infra.auth;

import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
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
    AuthServiceFacade authServiceFacade(
            AuthProperties authProperties,
            SessionStore sessionStore,
            OtpChallengeStore otpChallengeStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer,
            UserAuthRepository userAuthRepository,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender
    ) {
        return new AuthServiceFacade(
                authProperties,
                sessionStore,
                otpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                smsSendLogRepository,
                smsSender
        );
    }
}
