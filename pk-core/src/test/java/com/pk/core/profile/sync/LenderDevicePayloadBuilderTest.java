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
                        "Huawei", "P30", null, "12", null, 8, null, null, null, null, null, null, "1.2.3.4"
                )
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        assertEquals("LenderApp", payload.get("appName"));
        assertEquals("Huawei", payload.get("phoneBrand"));
        assertEquals("1.2.3.4", payload.get("ip"));
        @SuppressWarnings("unchecked")
        Map<String, Object> storedOther = (Map<String, Object>) payload.get("deviceOtherInfo");
        assertEquals("80", storedOther.get("battery"));
        assertFalse(storedOther.containsKey("unknownExtra"));
    }

    @Test
    void preservesValuesForDocumentedDeviceOtherInfoFields() {
        Map<String, Object> other = new LinkedHashMap<>();
        other.put("mac", "");
        other.put("cpuName", "   ");
        other.put("basebandVersion", null);
        other.put("isRoot", false);
        other.put("freeMemory", 0);
        other.put("wifiRSSI", -1);
        other.put("netModeName", "wifi");
        other.put("cameraSize", 48_000_000);
        other.put("blueMac", "");
        other.put("unknownExtra", "x");

        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp", "1.0.0", "com.example", "dev-1", "ios",
                null, other, "Client", DeviceExtendedAttributes.empty()
        );

        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        @SuppressWarnings("unchecked")
        Map<String, Object> storedOther = (Map<String, Object>) payload.get("deviceOtherInfo");
        assertEquals("", storedOther.get("mac"));
        assertEquals("   ", storedOther.get("cpuName"));
        assertTrue(storedOther.containsKey("basebandVersion"));
        assertEquals(null, storedOther.get("basebandVersion"));
        assertEquals(false, storedOther.get("isRoot"));
        assertEquals(0, storedOther.get("freeMemory"));
        assertEquals(-1, storedOther.get("wifiRSSI"));
        assertEquals("wifi", storedOther.get("netModeName"));
        assertEquals(48_000_000, storedOther.get("cameraSize"));
        assertEquals("", storedOther.get("blueMac"));
        assertFalse(storedOther.containsKey("unknownExtra"));
    }

    @Test
    void includesAdChannelWhenPresent() {
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp",
                "1.0.0",
                "com.example",
                "dev-1",
                "android",
                "ad-1",
                null,
                "ClientAppName",
                new DeviceExtendedAttributes(
                        null, null, null, null, null, null, null, null, null, null, null, "facebook", "1.2.3.4"
                )
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        assertEquals("facebook", payload.get("adChannel"));
        assertEquals("ad-1", payload.get("adId"));
        assertEquals("1.2.3.4", payload.get("ip"));
    }

    @Test
    void omitsIpWhenMissing() {
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp", "1.0.0", "com.example", "dev-1", "android",
                null, null, "Client", DeviceExtendedAttributes.empty()
        );
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        assertFalse(payload.containsKey("ip"));
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
