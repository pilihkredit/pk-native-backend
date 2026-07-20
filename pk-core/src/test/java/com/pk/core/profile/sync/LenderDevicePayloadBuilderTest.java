package com.pk.core.profile.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LenderDevicePayloadBuilderTest {
    @Test
    void buildsProfileSyncDeviceWithLenderAppNameAndFiltersOtherInfo() {
        Map<String, Object> other = new LinkedHashMap<>();
        other.put("battery", "80");
        other.put("unknownExtra", "x");
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp",
                "1.0.0",
                "com.example",
                "dev-1",
                "android",
                "ad-1",
                other,
                "ClientAppName",
                new DeviceExtendedAttributes(
                        "Huawei", "P30", null, "12", null, 8, null, null, null, null, null
                )
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        assertEquals("LenderApp", payload.get("appName"));
        assertEquals("Huawei", payload.get("phoneBrand"));
        @SuppressWarnings("unchecked")
        Map<String, Object> storedOther = (Map<String, Object>) payload.get("deviceOtherInfo");
        assertEquals("80", storedOther.get("battery"));
        assertFalse(storedOther.containsKey("unknownExtra"));
    }

    @Test
    void omitsDeviceOtherInfoWhenEmptyAfterFilter() {
        Map<String, Object> other = Map.of("unknownExtra", "x");
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp", "1.0.0", "com.example", "dev-1", "android",
                null, other, "Client", DeviceExtendedAttributes.empty()
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        assertFalse(payload.containsKey("deviceOtherInfo"));
    }

    @Test
    void riskApplyPutsEmptyDeviceOtherInfoWhenMissing() {
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp", "1.0.0", "com.example", "dev-1", "android",
                null, null, "Client", DeviceExtendedAttributes.empty()
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildRiskApplyDevice(device);
        assertTrue(payload.containsKey("deviceOtherInfo"));
        assertTrue(((Map<?, ?>) payload.get("deviceOtherInfo")).isEmpty());
    }
}
