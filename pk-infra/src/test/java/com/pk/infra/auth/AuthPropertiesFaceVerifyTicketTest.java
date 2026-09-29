package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class AuthPropertiesFaceVerifyTicketTest {

    @Test
    void usesDefaultTtlForAllScenesWhenOverridesUnset() {
        AuthProperties.FaceVerifyTicket ticket = new AuthProperties.FaceVerifyTicket();
        ticket.setTtl(Duration.ofMinutes(7));

        assertThat(ticket.deviceSwitchLoginTtl()).isEqualTo(Duration.ofMinutes(7));
        assertThat(ticket.bankCardAddTtl()).isEqualTo(Duration.ofMinutes(7));
        assertThat(ticket.mobileChangeTtl()).isEqualTo(Duration.ofMinutes(7));
    }

    @Test
    void appliesPerSceneOverrides() {
        AuthProperties.FaceVerifyTicket ticket = new AuthProperties.FaceVerifyTicket();
        ticket.setTtl(Duration.ofMinutes(5));
        ticket.setBankCardAddTtl(Duration.ofMinutes(15));
        ticket.setMobileChangeTtl(Duration.ofMinutes(10));

        assertThat(ticket.deviceSwitchLoginTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(ticket.bankCardAddTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(ticket.mobileChangeTtl()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void clampsInvalidTtlToSafeMinimum() {
        AuthProperties.FaceVerifyTicket ticket = new AuthProperties.FaceVerifyTicket();
        ticket.setTtl(Duration.ZERO);
        ticket.setDeviceSwitchLoginTtl(Duration.ofSeconds(30));

        assertThat(ticket.ttl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(ticket.deviceSwitchLoginTtl()).isEqualTo(Duration.ofMinutes(1));
    }
}
