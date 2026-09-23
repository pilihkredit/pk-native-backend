package com.pk.infra.auth;

import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import org.springframework.stereotype.Service;

/** Clears Redis session and refresh tokens so JWT access/refresh stop working immediately. */
@Service
public class UserSessionInvalidator {
    private final SessionStore sessionStore;
    private final RefreshTokenStore refreshTokenStore;

    public UserSessionInvalidator(SessionStore sessionStore, RefreshTokenStore refreshTokenStore) {
        this.sessionStore = sessionStore;
        this.refreshTokenStore = refreshTokenStore;
    }

    public void invalidateAll(long userId) {
        sessionStore.delete(userId);
        refreshTokenStore.deleteAllForProfile(userId);
    }
}
