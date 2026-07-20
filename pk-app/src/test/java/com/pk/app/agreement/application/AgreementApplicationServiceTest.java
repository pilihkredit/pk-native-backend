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
    void createUsesPrincipalPartnerUserIdWhenRequestOmitsIt() {
        AgreementFacade facade = mock(AgreementFacade.class);
        AgreementApplicationService service = new AgreementApplicationService(facade);
        when(facade.createRecords(any())).thenReturn(List.of(new UserAgreementRecordData(
                1L,
                "81234567890",
                "U10001",
                "device-1",
                10L,
                "PRIVACY_POLICY",
                true,
                Instant.parse("2026-07-20T03:00:00Z")
        )));

        var response = service.create(
                new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L),
                new AgreementCreateRequest(
                        "81234567890",
                        null,
                        "device-1",
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        );

        assertThat(response.records()).hasSize(1);
        ArgumentCaptor<AgreementFacade.CreateCommand> captor =
                ArgumentCaptor.forClass(AgreementFacade.CreateCommand.class);
        verify(facade).createRecords(captor.capture());
        assertThat(captor.getValue().partnerUserId()).isEqualTo("U10001");
        assertThat(captor.getValue().profileId()).isEqualTo(10L);
    }

    @Test
    void createRejectsPartnerUserIdMismatchWhenLoggedIn() {
        AgreementApplicationService service = new AgreementApplicationService(mock(AgreementFacade.class));

        assertThatThrownBy(() -> service.create(
                new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L),
                new AgreementCreateRequest(
                        "81234567890",
                        "OTHER",
                        "device-1",
                        List.of(new AgreementItemRequest("PRIVACY_POLICY", true))
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }
}
