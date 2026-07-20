package com.pk.infra.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.UserDeviceOtherInfoRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserDeviceWriterTest {
    private ProfileDeviceRepository profileDeviceRepository;
    private UserDeviceOtherInfoRepository userDeviceOtherInfoRepository;
    private UserDeviceWriter writer;

    @BeforeEach
    void setUp() {
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        userDeviceOtherInfoRepository = mock(UserDeviceOtherInfoRepository.class);
        writer = new UserDeviceWriter(profileDeviceRepository, userDeviceOtherInfoRepository, new ObjectMapper());
    }

    @Test
    void upsertsOtherInfoWhenDocumentFieldsPresentAndFiltersExtras() {
        Map<String, Object> other = new LinkedHashMap<>();
        other.put("battery", "80");
        other.put("unknownExtra", "x");
        writer.upsertFromRequest(10L, "PU1", "req-1", sampleDevice(other));

        ArgumentCaptor<ProfileDeviceData> deviceCaptor = ArgumentCaptor.forClass(ProfileDeviceData.class);
        verify(profileDeviceRepository).upsertByDeviceNo(deviceCaptor.capture());
        assertEquals("LenderApp", deviceCaptor.getValue().appName());
        assertEquals(true, deviceCaptor.getValue().deviceJson().contains("\"battery\":\"80\""));
        assertEquals(false, deviceCaptor.getValue().deviceJson().contains("unknownExtra"));

        verify(userDeviceOtherInfoRepository).upsert(argThat(data ->
                "80".equals(data.battery())
                        && data.deviceOtherInfoJson().contains("battery")
                        && !data.deviceOtherInfoJson().contains("unknownExtra")
        ));
        verify(userDeviceOtherInfoRepository, never()).deleteByDeviceNo(any());
    }

    @Test
    void deletesOtherInfoWhenOnlyExtensionKeysPresent() {
        writer.upsertFromRequest(10L, "PU1", "req-1", sampleDevice(Map.of("unknownExtra", "x")));

        verify(userDeviceOtherInfoRepository).deleteByDeviceNo("dev-1");
        verify(userDeviceOtherInfoRepository, never()).upsert(any());
    }

    private static LenderDeviceContext sampleDevice(Map<String, Object> other) {
        return new LenderDeviceContext(
                "LenderApp",
                "1.0.0",
                "com.example",
                "dev-1",
                "android",
                "ad-1",
                other,
                "ClientApp",
                DeviceExtendedAttributes.empty()
        );
    }
}
