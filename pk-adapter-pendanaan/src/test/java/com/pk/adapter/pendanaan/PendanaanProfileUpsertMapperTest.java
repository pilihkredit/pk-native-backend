package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PendanaanProfileUpsertMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsMobileNoToUserInfoRoot() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyMobileNo(userInfo, "81234567890");

        assertThat(userInfo.get("mobileNo").asText()).isEqualTo("81234567890");
    }

    @Test
    void mapsPersonalModuleToLenderProfileShape() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.PERSONAL,
                new ProfileSyncPayload.PersonalProfilePayload(
                        5,
                        16,
                        "5000000",
                        "Siti",
                        "user@example.com"
                )
        );

        assertThat(userInfo.get("profile").get("educationDegree").asInt()).isEqualTo(5);
        assertThat(userInfo.get("profile").get("industry").asInt()).isEqualTo(16);
        assertThat(userInfo.get("profile").get("income").asText()).isEqualTo("5000000");
        assertThat(userInfo.get("profile").get("motherSurname").asText()).isEqualTo("Siti");
        assertThat(userInfo.get("profile").get("userEmail").asText()).isEqualTo("user@example.com");
        assertThat(userInfo.has("job")).isFalse();
    }

    @Test
    void omitsUserEmailWhenBlank() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.PERSONAL,
                new ProfileSyncPayload.PersonalProfilePayload(
                        5,
                        1,
                        "8000000",
                        "Siti",
                        null
                )
        );

        assertThat(userInfo.get("profile").get("industry").asInt()).isEqualTo(1);
        assertThat(userInfo.get("profile").has("userEmail")).isFalse();
        assertThat(userInfo.has("job")).isFalse();
    }

    @Test
    void mapsContactModuleToLenderContactShape() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.CONTACT,
                new ProfileSyncPayload.ContactProfilePayload(
                        java.util.List.of(
                                new ProfileSyncPayload.ContactProfilePayload.ContactItem(
                                        0,
                                        "SITI",
                                        "81234567801"
                                )
                        )
                )
        );

        assertThat(userInfo.get("contact").get("userContacts")).hasSize(1);
        assertThat(userInfo.get("contact").get("userContacts").get(0).get("mobileNo").asText())
                .isEqualTo("81234567801");
        assertThat(userInfo.get("contact").get("userContacts").get(0).get("mobileName").asText())
                .isEqualTo("SITI");
    }

    @Test
    void mapsDeviceFields() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyDevice(
                userInfo,
                new LenderDeviceContext(
                        "PKApp",
                        "1.0.0",
                        "com.example.pk",
                        "device-1",
                        "android",
                        "ad-1",
                        Map.of("isRoot", false),
                        "KEC",
                        new DeviceExtendedAttributes(
                                "Huawei",
                                "P30",
                                "02:00:00:00:00:00",
                                "12",
                                "google play",
                                8,
                                8_000_000_000L,
                                64_000_000_000L,
                                null,
                                null,
                                null
                        )
                )
        );

        assertThat(userInfo.get("device").get("appName").asText()).isEqualTo("PKApp");
        assertThat(userInfo.get("device").get("deviceNo").asText()).isEqualTo("device-1");
        assertThat(userInfo.get("device").get("adId").asText()).isEqualTo("ad-1");
        assertThat(userInfo.get("device").get("phoneBrand").asText()).isEqualTo("Huawei");
        assertThat(userInfo.get("device").get("phoneBrandModel").asText()).isEqualTo("P30");
        assertThat(userInfo.get("device").get("systemVersion").asText()).isEqualTo("12");
        assertThat(userInfo.get("device").get("deviceOtherInfo").get("isRoot").asBoolean()).isFalse();
    }

    @Test
    void mapsLoginLogModuleToLenderLoginLogShape() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.LOGIN_LOG,
                new ProfileSyncPayload.LoginLogProfilePayload(
                        2,
                        "203.0.113.1",
                        new java.math.BigDecimal("-6.2088"),
                        new java.math.BigDecimal("106.8456")
                )
        );

        assertThat(userInfo.get("loginLog").get("loginType").asInt()).isEqualTo(2);
        assertThat(userInfo.get("loginLog").get("loginIp").asText()).isEqualTo("203.0.113.1");
        assertThat(userInfo.get("loginLog").get("loginLat").asText()).isEqualTo("-6.2088");
        assertThat(userInfo.get("loginLog").get("loginLng").asText()).isEqualTo("106.8456");
    }

    @Test
    void applyAppsFlyerInstallPutsNonBlankFieldsOnly() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.APPSFLYER_INSTALL,
                new ProfileSyncPayload.AppsFlyerInstallPayload(
                        "AF1", "AD1", null, null, null, "2026-07-08 10:00:00",
                        "media", null, null, null, null, null, "camp",
                        null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null
                )
        );
        assertThat(userInfo.path("appsFlyerInstall").path("appsflyerId").asText()).isEqualTo("AF1");
        assertThat(userInfo.path("appsFlyerInstall").path("advertisingId").asText()).isEqualTo("AD1");
        assertThat(userInfo.path("appsFlyerInstall").path("installTime").asText()).isEqualTo("2026-07-08 10:00:00");
        assertThat(userInfo.path("appsFlyerInstall").path("mediaSource").asText()).isEqualTo("media");
        assertThat(userInfo.path("appsFlyerInstall").path("campaign").asText()).isEqualTo("camp");
        assertThat(userInfo.path("appsFlyerInstall").has("androidId")).isFalse();
    }

    @Test
    void applyTongdunDevice() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.TONGDUN_DEVICE,
                new ProfileSyncPayload.TongdunDevicePayload("LOGIN", "KEY1")
        );
        assertThat(userInfo.path("tongdunDevice").path("sceneType").asText()).isEqualTo("LOGIN");
        assertThat(userInfo.path("tongdunDevice").path("tongdunKey").asText()).isEqualTo("KEY1");
    }
}
