package com.pk.infra.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserProfileRepositoryTest {
    @Test
    void formatsPartnerUserId() {
        assertThat(UserProfileRepository.formatPartnerUserId(1)).isEqualTo("U00001");
        assertThat(UserProfileRepository.formatPartnerUserId(10001)).isEqualTo("U10001");
    }
}
