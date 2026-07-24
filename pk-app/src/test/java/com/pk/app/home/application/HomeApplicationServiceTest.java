package com.pk.app.home.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.home.dto.request.HomeSummaryRequest;
import com.pk.app.profile.dto.request.ProfileDeviceRequest;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.home.HomeSummaryFacade;
import com.pk.infra.profile.UserDeviceWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HomeApplicationServiceTest {
    @Test
    void getSummaryUsesSubmittedDeviceAndPersistsIt() {
        HomeSummaryFacade homeSummaryFacade = mock(HomeSummaryFacade.class);
        UserDeviceWriter userDeviceWriter = mock(UserDeviceWriter.class);
        PendanaanProperties pendanaanProperties = new PendanaanProperties();
        pendanaanProperties.setAppName("PendanaanApp");
        HomeApplicationService service = new HomeApplicationService(
                homeSummaryFacade,
                pendanaanProperties,
                userDeviceWriter
        );
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getHeader("X-Device-No")).thenReturn("new-device");
        when(httpRequest.getHeader("X-App-Version")).thenReturn("1.0.0");
        when(httpRequest.getHeader("X-Platform")).thenReturn("android");
        when(httpRequest.getHeader("X-App-Package")).thenReturn("com.example");
        when(homeSummaryFacade.getSummary(eq(10L), eq("U10001"), eq("81234567890"), any()))
                .thenReturn(new HomeSummaryFacade.HomeSummaryResult(
                        "U10001",
                        "USR-1",
                        4,
                        22,
                        null,
                        true,
                        false,
                        false,
                        0,
                        1780300800000L,
                        true
                ));

        var response = service.getSummary(
                new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L),
                new HomeSummaryRequest(sampleDevice()),
                httpRequest
        );

        assertThat(response.partnerUserId()).isEqualTo("U10001");
        assertThat(response.firstLoan()).isTrue();
        assertThat(response.firstCreditApply()).isFalse();
        assertThat(response.firstLoanApply()).isFalse();
        assertThat(response.autoCredit()).isTrue();
        verify(userDeviceWriter).upsertFromRequest(
                eq(10L),
                eq("U10001"),
                any(String.class),
                any(LenderDeviceContext.class)
        );
        verify(homeSummaryFacade).getSummary(eq(10L), eq("U10001"), eq("81234567890"), any());
    }

    private static ProfileDeviceRequest sampleDevice() {
        return new ProfileDeviceRequest(
                "ClientApp",
                "1.0.0",
                "com.example",
                "new-device",
                "android",
                "Huawei",
                "P30",
                null,
                "12",
                "google play",
                8,
                8_000_000_000L,
                64_000_000_000L,
                "ad-id",
                null,
                null,
                null,
                null,
                "1.2.3.4",
                Map.of("isRoot", false)
        );
    }
}
