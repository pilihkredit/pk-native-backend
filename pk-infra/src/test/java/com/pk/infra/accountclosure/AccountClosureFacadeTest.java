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
import org.junit.jupiter.api.Test;

class AccountClosureFacadeTest {
    @Test
    void submitClosureDisablesLenderThenEnqueuesAndMarksProfileClosed() {
        AccountClosureMapper accountClosureMapper = mock(AccountClosureMapper.class);
        LenderUserDisablePort lenderUserDisablePort = mock(LenderUserDisablePort.class);
        AccountClosureLocalWriter localWriter = mock(AccountClosureLocalWriter.class);

        when(accountClosureMapper.countActiveDeletionQueue(10L)).thenReturn(0);
        when(localWriter.persistClosureLocally(
                new UserProfileSummary(10L, "U10", "81234567890", false),
                "cs1",
                null
        )).thenReturn(new AccountClosureFacade.AccountClosureSubmitResult(
                10L,
                "U10",
                "81234567890",
                "pending"
        ));

        var facade = new AccountClosureFacade(
                accountClosureMapper,
                lenderUserDisablePort,
                localWriter
        );

        var result = facade.submitClosureForUser(
                new UserProfileSummary(10L, "U10", "81234567890", false),
                null,
                "cs1"
        );

        verify(lenderUserDisablePort).disableUser("U10");
        verify(localWriter).persistClosureLocally(
                new UserProfileSummary(10L, "U10", "81234567890", false),
                "cs1",
                null
        );
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.queueStatus()).isEqualTo("pending");
    }

    @Test
    void submitClosureSkipsWhenQueueAlreadyActive() {
        AccountClosureMapper accountClosureMapper = mock(AccountClosureMapper.class);
        LenderUserDisablePort lenderUserDisablePort = mock(LenderUserDisablePort.class);
        when(accountClosureMapper.countActiveDeletionQueue(10L)).thenReturn(1);

        var facade = new AccountClosureFacade(
                accountClosureMapper,
                lenderUserDisablePort,
                mock(AccountClosureLocalWriter.class)
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
