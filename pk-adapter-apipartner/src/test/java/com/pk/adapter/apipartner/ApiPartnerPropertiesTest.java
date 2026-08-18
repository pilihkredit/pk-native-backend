package com.pk.adapter.apipartner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiPartnerPropertiesTest {

    @Test
    void httpCredentialsPresent_requiresAllFields() {
        ApiPartnerProperties properties = new ApiPartnerProperties();
        properties.setMode(ApiPartnerProperties.MODE_HTTP);
        properties.setBaseUrl("https://example.test");
        properties.setClientId("client");
        properties.setClientSecret("secret");
        properties.setAppName("app");

        assertThat(properties.httpCredentialsPresent()).isTrue();
        assertThatCode(properties::validateHttpSettings).doesNotThrowAnyException();
    }

    @Test
    void validateHttpSettings_failsWhenHttpModeWithoutCredentials() {
        ApiPartnerProperties properties = new ApiPartnerProperties();
        properties.setMode(ApiPartnerProperties.MODE_HTTP);

        assertThat(properties.httpCredentialsPresent()).isFalse();
        assertThatThrownBy(properties::validateHttpSettings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("base-url");
    }

    @Test
    void validateHttpSettings_ignoresFakeMode() {
        ApiPartnerProperties properties = new ApiPartnerProperties();
        properties.setMode(ApiPartnerProperties.MODE_FAKE);

        assertThatCode(properties::validateHttpSettings).doesNotThrowAnyException();
    }
}
