package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PendanaanProfileSyncAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsExternalUserIdFromLenderResponse() throws Exception {
        PendanaanHttpClient httpClient = mock(PendanaanHttpClient.class);
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("code", ApiCode.SUCCESS.code());
        ObjectNode data = envelope.putObject("data");
        data.put("userId", "USR202506020001");
        when(httpClient.postEnvelope(anyString(), anyString(), anyString(), anyString())).thenReturn(envelope);

        PendanaanProfileSyncAdapter adapter = new PendanaanProfileSyncAdapter(httpClient, objectMapper);
        LenderProfileSyncPort.LenderProfileSyncResult result = adapter.syncModule(sampleCommand());

        assertThat(result.externalUserId()).isEqualTo("USR202506020001");
        assertThat(result.responseDataJson()).contains("\"userId\":\"USR202506020001\"");
    }

    private LenderProfileSyncPort.LenderProfileSyncCommand sampleCommand() {
        return new LenderProfileSyncPort.LenderProfileSyncCommand(
                "REQ-1",
                "OPEN_USER_1",
                "81234567890",
                ProfileSyncModule.PERSONAL,
                new ProfileSyncPayload.PersonalProfilePayload(5, 16, "5000000", "Siti", "user@example.com"),
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
                                null,
                                "12",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                )
        );
    }

    @Test
    void buildsLenderUpsertBodyWithMobileNoProfileAndDevice() throws Exception {
        PendanaanHttpClient httpClient = mock(PendanaanHttpClient.class);
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("code", ApiCode.SUCCESS.code());
        when(httpClient.postEnvelope(anyString(), anyString(), anyString(), anyString())).thenReturn(envelope);

        PendanaanProfileSyncAdapter adapter = new PendanaanProfileSyncAdapter(httpClient, objectMapper);
        adapter.syncModule(sampleCommand());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient).postEnvelope(
                eq(PendanaanProfileSyncAdapter.UPSERT_PATH),
                bodyCaptor.capture(),
                eq(PendanaanProfileSyncAdapter.BUSINESS_TYPE),
                eq("OPEN_USER_1")
        );

        JsonNode root = objectMapper.readTree(bodyCaptor.getValue());
        assertThat(root.get("requestId").asText()).isEqualTo("REQ-1");
        assertThat(root.get("partnerUserId").asText()).isEqualTo("OPEN_USER_1");
        assertThat(root.get("userInfo").get("mobileNo").asText()).isEqualTo("81234567890");
        assertThat(root.get("userInfo").get("profile").get("educationDegree").asInt()).isEqualTo(5);
        assertThat(root.get("userInfo").get("profile").get("income").asText()).isEqualTo("5000000");
        assertThat(root.get("userInfo").get("device").get("appName").asText()).isEqualTo("PKApp");
        assertThat(root.get("userInfo").get("device").get("deviceNo").asText()).isEqualTo("device-1");
        assertThat(root.get("userInfo").has("job")).isFalse();
    }

    @Test
    void buildsLenderUpsertBodyWithLoginLogModule() throws Exception {
        PendanaanHttpClient httpClient = mock(PendanaanHttpClient.class);
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("code", ApiCode.SUCCESS.code());
        when(httpClient.postEnvelope(anyString(), anyString(), anyString(), anyString())).thenReturn(envelope);

        PendanaanProfileSyncAdapter adapter = new PendanaanProfileSyncAdapter(httpClient, objectMapper);
        adapter.syncModule(new LenderProfileSyncPort.LenderProfileSyncCommand(
                "REQ-LOGIN-1",
                "OPEN_USER_1",
                "81234567890",
                ProfileSyncModule.LOGIN_LOG,
                new ProfileSyncPayload.LoginLogProfilePayload(2, "203.0.113.1", null, null),
                sampleCommand().device()
        ));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient).postEnvelope(
                eq(PendanaanProfileSyncAdapter.UPSERT_PATH),
                bodyCaptor.capture(),
                eq(PendanaanProfileSyncAdapter.BUSINESS_TYPE),
                eq("OPEN_USER_1")
        );

        JsonNode root = objectMapper.readTree(bodyCaptor.getValue());
        assertThat(root.get("requestId").asText()).isEqualTo("REQ-LOGIN-1");
        assertThat(root.get("partnerUserId").asText()).isEqualTo("OPEN_USER_1");
        assertThat(root.get("userInfo").get("mobileNo").asText()).isEqualTo("81234567890");
        assertThat(root.get("userInfo").get("loginLog").get("loginType").asInt()).isEqualTo(2);
        assertThat(root.get("userInfo").get("loginLog").get("loginIp").asText()).isEqualTo("203.0.113.1");
        assertThat(root.get("userInfo").get("device").get("deviceNo").asText()).isEqualTo("device-1");
    }

    @Test
    void buildsLenderUpsertBodyWithIdentityAndAppsFlyerCompanion() throws Exception {
        PendanaanHttpClient httpClient = mock(PendanaanHttpClient.class);
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("code", ApiCode.SUCCESS.code());
        when(httpClient.postEnvelope(anyString(), anyString(), anyString(), anyString())).thenReturn(envelope);

        PendanaanProfileSyncAdapter adapter = new PendanaanProfileSyncAdapter(httpClient, objectMapper);
        adapter.syncModule(new LenderProfileSyncPort.LenderProfileSyncCommand(
                "REQ-ID-1",
                "OPEN_USER_1",
                "81234567890",
                ProfileSyncModule.IDENTITY,
                new ProfileSyncPayload.IdentityProfilePayload(
                        "Name",
                        "3174",
                        "face",
                        "idcard",
                        "{}",
                        "advanceAi",
                        "Name",
                        "3174",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                sampleCommand().device(),
                List.of(new LenderProfileSyncPort.SyncCompanion(
                        ProfileSyncModule.APPSFLYER_INSTALL,
                        new ProfileSyncPayload.AppsFlyerInstallPayload(
                                "AF1", "AD1", null, null, null, "2026-07-08 10:00:00",
                                "media", null, null, null, null, null, "camp",
                                null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null
                        ),
                        "af-req-1"
                ))
        ));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient).postEnvelope(
                eq(PendanaanProfileSyncAdapter.UPSERT_PATH),
                bodyCaptor.capture(),
                eq(PendanaanProfileSyncAdapter.BUSINESS_TYPE),
                eq("OPEN_USER_1")
        );

        JsonNode root = objectMapper.readTree(bodyCaptor.getValue());
        assertThat(root.get("userInfo").get("identity").get("name").asText()).isEqualTo("Name");
        assertThat(root.get("userInfo").get("appsFlyerInstall").get("appsflyerId").asText()).isEqualTo("AF1");
        assertThat(root.get("userInfo").get("appsFlyerInstall").get("campaign").asText()).isEqualTo("camp");
    }
}
