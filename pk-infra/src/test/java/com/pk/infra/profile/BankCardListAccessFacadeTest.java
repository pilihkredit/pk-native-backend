package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankCardListAccessFacadeTest {
    private ProfileQueryFacade profileQueryFacade;
    private LenderUserStatusPort lenderUserStatusPort;
    private BankCardListAccessFacade facade;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        profileQueryFacade = mock(ProfileQueryFacade.class);
        lenderUserStatusPort = mock(LenderUserStatusPort.class);
        facade = new BankCardListAccessFacade(profileQueryFacade, lenderUserStatusPort);
    }

    @Test
    void allowsWhenHasCardAndStatusNotBlocked() {
        when(profileQueryFacade.query(eq("U1"), eq(List.of("bankCard")))).thenReturn(bankCardListJson(true));
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(4));

        var result = facade.checkAccess("U1", sampleDevice());

        assertThat(result.canShowList()).isTrue();
    }

    @Test
    void deniesWhenNoBankCard() {
        when(profileQueryFacade.query(eq("U1"), eq(List.of("bankCard")))).thenReturn(bankCardListJson(false));

        assertThatThrownBy(() -> facade.checkAccess("U1", sampleDevice()))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_LIST_ACCESS_DENIED);
    }

    @Test
    void deniesWhenStatusBlocked() {
        when(profileQueryFacade.query(eq("U1"), eq(List.of("bankCard")))).thenReturn(bankCardListJson(true));
        when(lenderUserStatusPort.queryStatus(any())).thenReturn(statusResult(3));

        assertThatThrownBy(() -> facade.checkAccess("U1", sampleDevice()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiException = (ApiException) ex;
                    assertThat(apiException.apiCode()).isEqualTo(ApiCode.BANK_CARD_LIST_ACCESS_DENIED);
                    assertThat(apiException.apiCode().message())
                            .isEqualTo("Status saat ini tidak memungkinkan modifikasi, silakan coba lagi nanti");
                });
    }

    private ObjectNode bankCardListJson(boolean withCard) {
        ObjectNode root = objectMapper.createObjectNode();
        var list = root.putArray("bankCardList");
        if (withCard) {
            list.addObject().put("bankCardId", 1).put("cardNumber", "123");
        }
        return root;
    }

    private static LenderUserStatusPort.LenderUserStatusResult statusResult(Integer status) {
        return new LenderUserStatusPort.LenderUserStatusResult(
                "U1",
                "USR1",
                status,
                22,
                null,
                true,
                true,
                true,
                0,
                null,
                false,
                "{}",
                "{}"
        );
    }

    private static LenderDeviceContext sampleDevice() {
        return new LenderDeviceContext(
                "PKApp",
                "1.0.0",
                "com.example.pk",
                "device-1",
                "android",
                null,
                null,
                "KEC"
        );
    }
}
