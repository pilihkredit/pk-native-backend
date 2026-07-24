package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PendanaanDeviceNodeBuilderTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void riskApplyDeviceAlwaysIncludesDeviceOtherInfoObject() throws Exception {
        String body = PendanaanCreditRequestMapper.buildApplyBody(new LenderCreditPort.LenderCreditApplyCommand(
                "apply-1",
                "partner-1",
                null,
                null,
                null,
                null,
                sampleDevice(null),
                List.of()
        ));

        JsonNode openUserDevice = objectMapper.readTree(body).path("riskDataInfo").path("openUserDevice");
        assertThat(openUserDevice.path("deviceOtherInfo").isObject()).isTrue();
        assertThat(openUserDevice.path("phoneBrand").asText()).isEqualTo("Apple");
    }

    @Test
    void profileSyncDeviceOmitsEmptyDeviceOtherInfo() {
        JsonNode device = PendanaanDeviceNodeBuilder.buildProfileSyncDevice(sampleDevice(null));
        assertThat(device.has("deviceOtherInfo")).isFalse();
    }

    private static LenderDeviceContext sampleDevice(Map<String, Object> deviceOtherInfo) {
        return new LenderDeviceContext(
                "PKApp",
                "1.0.0",
                "com.example.pk",
                "device-1",
                "android",
                "ad-1",
                deviceOtherInfo,
                "KEC",
                new DeviceExtendedAttributes(
                        "Apple",
                        "iPhone 14",
                        null,
                        "17.0",
                        "App Store",
                        null,
                        null,
                        null,
                        "idfv-1",
                        "idfa-1",
                        null,
                        null,
                        null
                )
        );
    }
}
