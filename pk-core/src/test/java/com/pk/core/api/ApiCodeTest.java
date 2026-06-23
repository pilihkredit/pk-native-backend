package com.pk.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApiCodeTest {
    @Test
    void exposesSuccessCodeAndMessage() {
        assertEquals("000000", ApiCode.SUCCESS.code());
        assertEquals("success", ApiCode.SUCCESS.message());
        assertTrue(ApiCode.SUCCESS.success());
    }

    @Test
    void classifiesPublicCodeLayers() {
        assertEquals(ApiCodeLayer.PLATFORM_VALIDATION, ApiCode.INVALID_REQUEST_PARAMETERS.layer());
        assertEquals(ApiCodeLayer.UPSTREAM_BUSINESS, ApiCode.UPSTREAM_APPLICATION_NOT_FOUND.layer());
        assertEquals(ApiCodeLayer.PLATFORM_ORCHESTRATION, ApiCode.QUOTE_SNAPSHOT_EXPIRED.layer());
        assertEquals(ApiCodeLayer.SYSTEM, ApiCode.SERVICE_UNAVAILABLE.layer());
    }

    @Test
    void rejectsUpstreamRawCodeAsPublicCode() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ApiCode.fromPublicCode("A000001")
        );
        assertTrue(error.getMessage().contains("Raw upstream codes are not public API codes"));
    }

    @Test
    void resolvesKnownCodeByValue() {
        assertEquals(ApiCode.INVALID_REQUEST_PARAMETERS, ApiCode.fromPublicCode("K000001"));
        assertEquals(ApiCode.INTERNAL_SERVER_ERROR, ApiCode.fromPublicCode("999999"));
    }
}
