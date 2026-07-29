package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;

final class PendanaanDeviceNodeBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LENDER_APP_NAME = "PilihKredit";

    private PendanaanDeviceNodeBuilder() {
    }

    static ObjectNode buildProfileSyncDevice(LenderDeviceContext device) {
        return withLenderAppName(
                OBJECT_MAPPER.valueToTree(LenderDevicePayloadBuilder.buildProfileSyncDevice(device))
        );
    }

    static ObjectNode buildRiskApplyDevice(LenderDeviceContext device) {
        return withLenderAppName(
                OBJECT_MAPPER.valueToTree(LenderDevicePayloadBuilder.buildRiskApplyDevice(device))
        );
    }

    private static ObjectNode withLenderAppName(ObjectNode deviceNode) {
        deviceNode.put("appName", LENDER_APP_NAME);
        return deviceNode;
    }
}
