package com.pk.app.ops.support;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.app.ops.config.OpsBackofficeProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class OpsBackofficeAuthSupportTest {
    @Test
    void rejectsWhenDisabled() {
        OpsBackofficeProperties properties = new OpsBackofficeProperties();
        properties.setEnabled(false);
        properties.setToken("secret");
        var support = new OpsBackofficeAuthSupport(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> support.requireAuthorized(request, "secret"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.OPS_API_DISABLED);
    }

    @Test
    void acceptsMatchingTokenAndIp() {
        OpsBackofficeProperties properties = new OpsBackofficeProperties();
        properties.setEnabled(true);
        properties.setToken("secret");
        properties.allowedClientIps().add("127.0.0.1");
        var support = new OpsBackofficeAuthSupport(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        assertThatCode(() -> support.requireAuthorized(request, "secret"))
                .doesNotThrowAnyException();
    }
}
