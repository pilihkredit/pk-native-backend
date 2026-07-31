package com.pk.infra.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DevicePayloadJsonSupportTest {
    @Test
    void toLenderDeviceJsonMatchesCoreBuilder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
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
                "ClientApp",
                new DeviceExtendedAttributes("Huawei", "P30", null, "12", null, 8, null, null, null, null, null, null, "1.2.3.4")
        );
        String stored = DevicePayloadJsonSupport.toLenderDeviceJson(device, mapper);
        assertEquals(
                mapper.valueToTree(LenderDevicePayloadBuilder.buildProfileSyncDevice(device)),
                mapper.readTree(stored)
        );
    }

    @Test
    void preservesDocumentedValuesInStoredDeviceJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> other = new LinkedHashMap<>();
        other.put("macAddress", "");
        other.put("basebandVersion", null);
        other.put("cameraSize", 48_000_000);
        other.put("unknownExtra", "x");
        LenderDeviceContext device = new LenderDeviceContext(
                "LenderApp", "1.0.0", "com.example", "dev-1", "ios",
                null, other, "ClientApp", DeviceExtendedAttributes.empty()
        );

        JsonNode storedOther = mapper.readTree(DevicePayloadJsonSupport.toLenderDeviceJson(device, mapper))
                .path("deviceOtherInfo");

        assertEquals("", storedOther.path("macAddress").asText());
        assertTrue(storedOther.has("basebandVersion"));
        assertTrue(storedOther.get("basebandVersion").isNull());
        assertEquals(48_000_000, storedOther.path("cameraSize").asInt());
        assertFalse(storedOther.has("unknownExtra"));
    }
}
