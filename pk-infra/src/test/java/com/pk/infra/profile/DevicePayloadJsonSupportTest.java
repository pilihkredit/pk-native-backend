package com.pk.infra.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                new DeviceExtendedAttributes("Huawei", "P30", null, "12", null, 8, null, null, null, null, null)
        );
        String stored = DevicePayloadJsonSupport.toLenderDeviceJson(device, mapper);
        assertEquals(
                mapper.valueToTree(LenderDevicePayloadBuilder.buildProfileSyncDevice(device)),
                mapper.readTree(stored)
        );
    }
}
