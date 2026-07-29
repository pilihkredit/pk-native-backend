package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.loan.port.LenderLoanApplyPort;
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
                sampleAppList()
        ));

        JsonNode riskDataInfo = objectMapper.readTree(body).path("riskDataInfo");
        JsonNode openUserDevice = riskDataInfo.path("openUserDevice");
        assertThat(openUserDevice.path("appName").asText()).isEqualTo("PKApp");
        assertThat(openUserDevice.path("deviceOtherInfo").isObject()).isTrue();
        assertThat(openUserDevice.path("phoneBrand").asText()).isEqualTo("Apple");
        assertThat(riskDataInfo.has("appList")).isTrue();
        assertThat(riskDataInfo.path("appList").path(0).path("appName").asText()).isEqualTo("Example App");
    }

    @Test
    void profileSyncDeviceOmitsEmptyDeviceOtherInfo() {
        JsonNode device = PendanaanDeviceNodeBuilder.buildProfileSyncDevice(sampleDevice(null));
        assertThat(device.path("appName").asText()).isEqualTo("PKApp");
        assertThat(device.has("deviceOtherInfo")).isFalse();
    }

    @Test
    void creditApplyOmitsAppListForIos() throws Exception {
        String body = PendanaanCreditRequestMapper.buildApplyBody(new LenderCreditPort.LenderCreditApplyCommand(
                "apply-1",
                "partner-1",
                null,
                null,
                null,
                null,
                sampleDevice("ios", null),
                sampleAppList()
        ));

        JsonNode riskDataInfo = objectMapper.readTree(body).path("riskDataInfo");
        assertThat(riskDataInfo.has("appList")).isFalse();
    }

    @Test
    void loanApplyOmitsAppListForIos() throws Exception {
        String body = PendanaanLoanRequestMapper.buildApplyBody(new LenderLoanApplyPort.LenderLoanApplyCommand(
                "apply-1",
                "loan-1",
                null,
                "product-1",
                "method-1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                sampleDevice("ios", null),
                sampleAppList()
        ));

        JsonNode riskDataInfo = objectMapper.readTree(body).path("riskDataInfo");
        assertThat(riskDataInfo.has("appList")).isFalse();
    }

    private static LenderDeviceContext sampleDevice(Map<String, Object> deviceOtherInfo) {
        return sampleDevice("android", deviceOtherInfo);
    }

    private static LenderDeviceContext sampleDevice(String platform, Map<String, Object> deviceOtherInfo) {
        return new LenderDeviceContext(
                "PKApp",
                "1.0.0",
                "com.example.pk",
                "device-1",
                platform,
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

    private static List<CreditRiskAppInfo> sampleAppList() {
        return List.of(new CreditRiskAppInfo(
                "Example App",
                "com.example.app",
                1,
                1,
                "1",
                "1.0.0",
                1L,
                2L
        ));
    }
}
