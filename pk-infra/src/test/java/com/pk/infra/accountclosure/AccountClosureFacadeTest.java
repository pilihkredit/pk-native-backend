package com.pk.infra.accountclosure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.LenderUserDisablePort;
import com.pk.infra.accountclosure.mapper.AccountClosureMapper;
import com.pk.infra.auth.mapper.UserAuthMapper;
import org.junit.jupiter.api.Test;

class AccountClosureFacadeTest {
    @Test
    void submitClosureDisablesLenderThenEnqueuesAndMarksProfileClosed() {
        UserAuthMapper userAuthMapper = mock(UserAuthMapper.class);
        AccountClosureMapper accountClosureMapper = mock(AccountClosureMapper.class);
        LenderUserDisablePort lenderUserDisablePort = mock(LenderUserDisablePort.class);

        when(accountClosureMapper.countActiveDeletionQueue(10L)).thenReturn(0);
        when(accountClosureMapper.insertDeletionQueue(10L, "U10", "81234567890", "operator=cs1"))
                .thenReturn(1);
        when(userAuthMapper.markAccountClosed(10L)).thenReturn(1);

        var facade = new AccountClosureFacade(
                userAuthMapper,
                accountClosureMapper,
                lenderUserDisablePort
        );

        var result = facade.submitClosureForUser(
                new UserProfileSummary(10L, "U10", "81234567890", false),
                null,
                "cs1"
        );

        verify(lenderUserDisablePort).disableUser("U10");
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.queueStatus()).isEqualTo("pending");
    }

    @Test
    void submitClosureSkipsWhenQueueAlreadyActive() {
        AccountClosureMapper accountClosureMapper = mock(AccountClosureMapper.class);
        LenderUserDisablePort lenderUserDisablePort = mock(LenderUserDisablePort.class);
        when(accountClosureMapper.countActiveDeletionQueue(10L)).thenReturn(1);

        var facade = new AccountClosureFacade(
                mock(UserAuthMapper.class),
                accountClosureMapper,
                lenderUserDisablePort
        );

        assertThatThrownBy(() -> facade.submitClosureForUser(
                new UserProfileSummary(10L, "U10", "81234567890", false),
                "reason",
                "cs1"
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.ACCOUNT_CLOSURE_ALREADY_REQUESTED);

        verify(lenderUserDisablePort, never()).disableUser(any());
    }
}
