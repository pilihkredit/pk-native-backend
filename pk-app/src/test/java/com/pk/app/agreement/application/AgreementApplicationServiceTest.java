package com.pk.app.agreement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.agreement.dto.request.AgreementCreateRequest;
import com.pk.app.agreement.dto.request.AgreementItemRequest;
import com.pk.core.agreement.UserAgreementRecordData;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.agreement.AgreementFacade;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AgreementApplicationServiceTest {
    @Test
    void createUsesPrincipalMobileAndPartnerUserId() {
        AgreementFacade facade = mock(AgreementFacade.class);
        AgreementApplicationService service = new AgreementApplicationService(facade);
        long clickedAt = Instant.parse("2026-07-20T03:00:00Z").toEpochMilli();
        when(facade.createRecords(any())).thenReturn(List.of(new UserAgreementRecordData(
                1L,
                "U10001",
                "device-1",
                10L,
                "PRIVACY_POLICY",
                true,
                Instant.ofEpochMilli(clickedAt),
                clickedAt,
                "81234567890"
        )));

        var response = service.create(
                new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L),
                new AgreementCreateRequest(
                        null,
                        "device-1",
                        clickedAt,
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        );

        assertThat(response.records()).hasSize(1);
        ArgumentCaptor<AgreementFacade.CreateCommand> captor =
                ArgumentCaptor.forClass(AgreementFacade.CreateCommand.class);
        verify(facade).createRecords(captor.capture());
        assertThat(captor.getValue().mobileNo()).isEqualTo("81234567890");
        assertThat(captor.getValue().partnerUserId()).isEqualTo("U10001");
        assertThat(captor.getValue().userId()).isEqualTo(10L);
        assertThat(captor.getValue().clickedAtMs()).isEqualTo(clickedAt);
    }

    @Test
    void createAllowsAnonymousWithoutPartnerUserIdOrMobile() {
        AgreementFacade facade = mock(AgreementFacade.class);
        AgreementApplicationService service = new AgreementApplicationService(facade);
        when(facade.createRecords(any())).thenReturn(List.of());

        long clickedAt = 1_721_440_800_000L;
        service.create(
                null,
                new AgreementCreateRequest(
                        null,
                        "device-1",
                        clickedAt,
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        );

        ArgumentCaptor<AgreementFacade.CreateCommand> captor =
                ArgumentCaptor.forClass(AgreementFacade.CreateCommand.class);
        verify(facade).createRecords(captor.capture());
        assertThat(captor.getValue().mobileNo()).isNull();
        assertThat(captor.getValue().partnerUserId()).isNull();
        assertThat(captor.getValue().userId()).isNull();
        assertThat(captor.getValue().deviceNo()).isEqualTo("device-1");
        assertThat(captor.getValue().clickedAtMs()).isEqualTo(clickedAt);
    }

    @Test
    void createKeepsOptionalPartnerUserIdWhenAnonymous() {
        AgreementFacade facade = mock(AgreementFacade.class);
        AgreementApplicationService service = new AgreementApplicationService(facade);
        when(facade.createRecords(any())).thenReturn(List.of());

        service.create(
                null,
                new AgreementCreateRequest(
                        "U10001",
                        "device-1",
                        1_721_440_800_000L,
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        );

        ArgumentCaptor<AgreementFacade.CreateCommand> captor =
                ArgumentCaptor.forClass(AgreementFacade.CreateCommand.class);
        verify(facade).createRecords(captor.capture());
        assertThat(captor.getValue().partnerUserId()).isEqualTo("U10001");
        assertThat(captor.getValue().mobileNo()).isNull();
        assertThat(captor.getValue().userId()).isNull();
    }

    @Test
    void createRejectsPartnerUserIdMismatchWhenLoggedIn() {
        AgreementApplicationService service = new AgreementApplicationService(mock(AgreementFacade.class));

        assertThatThrownBy(() -> service.create(
                new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L),
                new AgreementCreateRequest(
                        "OTHER",
                        "device-1",
                        1_721_440_800_000L,
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }
}
