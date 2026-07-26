package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MobileChangeFacadeTest {
    private UserAuthRepository userAuthRepository;
    private UserMobileChangeLogRepository userMobileChangeLogRepository;
    private SessionStore sessionStore;
    private RefreshTokenStore refreshTokenStore;
    private MobileChangeFacade facade;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        userMobileChangeLogRepository = mock(UserMobileChangeLogRepository.class);
        sessionStore = mock(SessionStore.class);
        refreshTokenStore = mock(RefreshTokenStore.class);
        facade = new MobileChangeFacade(
                userAuthRepository,
                userMobileChangeLogRepository,
                sessionStore,
                refreshTokenStore
        );
    }

    @Test
    void rejectsWhenNewMobileOwnedByAnotherUser() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(summary(1L, "U1", "81111111111")));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.of(summary(2L, "U2", "81222222222")));

        assertThatThrownBy(() -> facade.changeMobile(1L, "81222222222"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.MOBILE_ALREADY_REGISTERED);
    }

    @Test
    void updatesMasterInsertsLogAndClearsTokens() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(summary(1L, "U1", "81111111111")));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());

        MobileChangeFacade.MobileChangeResult result = facade.changeMobile(1L, "81222222222");

        assertThat(result.changed()).isTrue();
        assertThat(result.mobileNo()).isEqualTo("81222222222");
        verify(userAuthRepository).updateMobileNo(1L, "81222222222");
        verify(userMobileChangeLogRepository).insert(argThat(e ->
                e.userId() == 1L
                        && e.oldMobileNo().equals("81111111111")
                        && e.newMobileNo().equals("81222222222")
                        && e.status().equals("SUCCESS")
                        && e.operatorType().equals("USER")));
        verify(sessionStore).delete(1L);
        verify(refreshTokenStore).deleteAllForProfile(1L);
        verify(userAuthRepository).clearSessionTokens(1L);
        verify(userAuthRepository).updateLastLogoutAt(eq(1L), any());
    }

    @Test
    void sameMobileReturnsChangedFalseWithoutSideEffects() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(summary(1L, "U1", "81111111111")));

        MobileChangeFacade.MobileChangeResult result = facade.changeMobile(1L, "81111111111");

        assertThat(result.changed()).isFalse();
        verify(userAuthRepository, never()).updateMobileNo(anyLong(), any());
        verify(userMobileChangeLogRepository, never()).insert(any());
        verify(userAuthRepository, never()).clearSessionTokens(anyLong());
    }

    private static UserProfileSummary summary(long userId, String partnerUserId, String mobileNo) {
        return new UserProfileSummary(userId, partnerUserId, mobileNo, false);
    }
}
