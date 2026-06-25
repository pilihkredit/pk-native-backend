package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import org.junit.jupiter.api.Test;

class PendanaanProfileUpsertMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsWorkModuleToLenderJobShape() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.WORK,
                new ProfileSyncPayload.WorkProfilePayload(
                        1,
                        "PT Example",
                        "31",
                        "3171",
                        "317101",
                        "Jl. Thamrin",
                        "5000000",
                        25,
                        2
                )
        );

        assertThat(userInfo.get("job").get("industry").asInt()).isEqualTo(1);
        assertThat(userInfo.get("job").get("name").asText()).isEqualTo("PT Example");
        assertThat(userInfo.get("job").get("provinceId").asInt()).isEqualTo(31);
        assertThat(userInfo.get("job").get("income").asText()).isEqualTo("5000000");
        assertThat(userInfo.get("job").get("payday").asInt()).isEqualTo(25);
    }

    @Test
    void mapsPersonalModuleToLenderProfileShape() {
        ObjectNode userInfo = objectMapper.createObjectNode();
        PendanaanProfileUpsertMapper.applyModule(
                userInfo,
                ProfileSyncModule.PERSONAL,
                new ProfileSyncPayload.PersonalProfilePayload(
                        "31",
                        "3171",
                        "317101",
                        "Jl. Example",
                        5,
                        "Siti"
                )
        );

        assertThat(userInfo.get("profile").get("provinceId").asInt()).isEqualTo(31);
        assertThat(userInfo.get("profile").get("cityId").asInt()).isEqualTo(3171);
        assertThat(userInfo.get("profile").get("districtId").asInt()).isEqualTo(317101);
        assertThat(userInfo.get("profile").get("motherSurname").asText()).isEqualTo("Siti");
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
                new LenderDeviceContext("PKApp", "1.0.0", "com.example.pk", "device-1", "android", "ad-1", null, "KEC")
        );

        assertThat(userInfo.get("device").get("appName").asText()).isEqualTo("PKApp");
        assertThat(userInfo.get("device").get("deviceNo").asText()).isEqualTo("device-1");
        assertThat(userInfo.get("device").get("adId").asText()).isEqualTo("ad-1");
    }
}
