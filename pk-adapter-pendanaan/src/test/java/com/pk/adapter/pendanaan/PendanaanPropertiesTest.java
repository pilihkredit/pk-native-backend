package com.pk.adapter.pendanaan;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendanaanPropertiesTest {

    @Test
    void httpCredentialsPresent_requiresAllFields() {
        PendanaanProperties properties = new PendanaanProperties();
        properties.setMode(PendanaanProperties.MODE_HTTP);
        properties.setBaseUrl("https://example.test");
        properties.setClientId("client");
        properties.setClientSecret("secret");
        properties.setAppName("app");

        assertThat(properties.httpCredentialsPresent()).isTrue();
        assertThatCode(properties::validateHttpSettings).doesNotThrowAnyException();
    }

    @Test
    void validateHttpSettings_failsWhenHttpModeWithoutCredentials() {
        PendanaanProperties properties = new PendanaanProperties();
        properties.setMode(PendanaanProperties.MODE_HTTP);

        assertThat(properties.httpCredentialsPresent()).isFalse();
        assertThatThrownBy(properties::validateHttpSettings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("base-url");
    }

    @Test
    void validateHttpSettings_ignoresFakeMode() {
        PendanaanProperties properties = new PendanaanProperties();
        properties.setMode(PendanaanProperties.MODE_FAKE);

        assertThatCode(properties::validateHttpSettings).doesNotThrowAnyException();
    }
}
