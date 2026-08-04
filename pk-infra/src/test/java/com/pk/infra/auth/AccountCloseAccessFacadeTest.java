package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import org.junit.jupiter.api.Test;

class AccountCloseAccessFacadeTest {
    @Test
    void allowsAccountCloseForACompletedLoanWithoutOutstandingLoans() {
        LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(4, 0));

        var result = new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device());

        assertThat(result.canClose()).isTrue();
    }

    @Test
    void rejectsBlockedLifetimeStatuses() {
        for (int blockedStatus : new int[] {3, 5, 6, 7}) {
            LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
            when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(blockedStatus, 0));

            assertThatThrownBy(() -> new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device()))
                    .isInstanceOf(ApiException.class)
                    .extracting("apiCode")
                    .isEqualTo(ApiCode.ACCOUNT_CLOSE_NOT_ALLOWED);
        }
    }

    @Test
    void rejectsWhenThereAreOutstandingLoans() {
        LenderUserStatusPort lenderUserStatusPort = mock(LenderUserStatusPort.class);
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(4, 1));

        assertThatThrownBy(() -> new AccountCloseAccessFacade(lenderUserStatusPort).checkAccess(principal(), device()))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.ACCOUNT_CLOSE_NOT_ALLOWED);
    }

    private static AuthenticatedPrincipal principal() {
        return new AuthenticatedPrincipal(1L, "U1", "8123456789", 1L);
    }

    private static LenderDeviceContext device() {
        return new LenderDeviceContext("PKApp", "1.0.0", "com.example.pk", "device-1", "android");
    }

    private static LenderUserStatusPort.LenderUserStatusResult statusResult(Integer status, Integer onLoanCount) {
        return new LenderUserStatusPort.LenderUserStatusResult(
                "U1", "USR1", status, null, null, null, null, null, onLoanCount, null, null, null
        );
    }
}
