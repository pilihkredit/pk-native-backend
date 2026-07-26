package com.pk.app.tracking.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.app.tracking.dto.request.TrackingEventRequest;
import com.pk.core.auth.AuthenticatedPrincipal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrackingApplicationServiceResolveIdentityTest {
    @Test
    void anonymousWithoutBearerKeepsIdsNull() {
        var identity = TrackingApplicationService.resolveIdentity(null, false, sampleRequest());
        assertThat(identity.userId()).isNull();
        assertThat(identity.partnerUserId()).isNull();
    }

    @Test
    void loggedInPrincipalFillsBothIds() {
        var principal = new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L);
        var identity = TrackingApplicationService.resolveIdentity(principal, true, sampleRequest());
        assertThat(identity.userId()).isEqualTo(10L);
        assertThat(identity.partnerUserId()).isEqualTo("U10001");
    }

    @Test
    void loggedInButBlankPartnerUserIdLeavesPartnerNull() {
        var principal = new AuthenticatedPrincipal(10L, "  ", "81234567890", 1L);
        var identity = TrackingApplicationService.resolveIdentity(principal, true, sampleRequest());
        assertThat(identity.userId()).isEqualTo(10L);
        assertThat(identity.partnerUserId()).isNull();
    }

    private static TrackingEventRequest sampleRequest() {
        return new TrackingEventRequest(
                1L,
                null,
                "page_view",
                "/home",
                Map.of(),
                "trace-1",
                "device-1",
                "Apple",
                "iPhone",
                "phone",
                "iOS",
                "18.0",
                "com.pk.app",
                "1.0.0",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
