package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;

public final class DevicePayloadJsonSupport {
    private DevicePayloadJsonSupport() {
    }

    /** Serializes the same shape as lender {@code userInfo.device} / profile-sync device. */
    public static String toLenderDeviceJson(LenderDeviceContext device, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(LenderDevicePayloadBuilder.buildProfileSyncDevice(device));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize lender device JSON", exception);
        }
    }

    /** @deprecated use {@link #toLenderDeviceJson(LenderDeviceContext, ObjectMapper)} */
    @Deprecated
    public static String toClientDeviceJson(LenderDeviceContext device, ObjectMapper objectMapper) {
        return toLenderDeviceJson(device, objectMapper);
    }
}
