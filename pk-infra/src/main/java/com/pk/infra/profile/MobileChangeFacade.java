package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository.UserMobileChangeLogEntry;
import com.pk.infra.auth.MobileNumberValidator;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileChangeFacade {
    private final UserAuthRepository userAuthRepository;
    private final UserMobileChangeLogRepository userMobileChangeLogRepository;
    private final SessionStore sessionStore;
    private final RefreshTokenStore refreshTokenStore;

    public MobileChangeFacade(
            UserAuthRepository userAuthRepository,
            UserMobileChangeLogRepository userMobileChangeLogRepository,
            SessionStore sessionStore,
            RefreshTokenStore refreshTokenStore
    ) {
        this.userAuthRepository = userAuthRepository;
        this.userMobileChangeLogRepository = userMobileChangeLogRepository;
        this.sessionStore = sessionStore;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public MobileChangeResult changeMobile(long userId, String rawNewMobile) {
        String newMobile = normalizeMobile(rawNewMobile);
        if (!MobileNumberValidator.isValid(newMobile)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }

        UserProfileSummary current = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (newMobile.equals(current.mobileNo())) {
            return new MobileChangeResult(false, newMobile);
        }
        if (userAuthRepository.findActiveByMobileNoExcludingUserId(newMobile, userId).isPresent()) {
            throw new ApiException(ApiCode.MOBILE_ALREADY_REGISTERED);
        }

        userAuthRepository.updateMobileNo(userId, newMobile);
        userMobileChangeLogRepository.insert(new UserMobileChangeLogEntry(
                userId,
                current.partnerUserId(),
                current.mobileNo(),
                newMobile,
                "SUCCESS",
                "USER",
                null,
                null
        ));
        sessionStore.delete(userId);
        refreshTokenStore.deleteAllForProfile(userId);
        userAuthRepository.clearSessionTokens(userId);
        userAuthRepository.updateLastLogoutAt(userId, Instant.now());
        return new MobileChangeResult(true, newMobile);
    }

    private static String normalizeMobile(String mobileNo) {
        return mobileNo == null ? "" : mobileNo.trim();
    }

    public record MobileChangeResult(
            boolean changed,
            String mobileNo
    ) {
    }
}
