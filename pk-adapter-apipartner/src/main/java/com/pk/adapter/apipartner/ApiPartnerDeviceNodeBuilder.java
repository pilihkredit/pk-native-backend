package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;

final class ApiPartnerDeviceNodeBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ApiPartnerDeviceNodeBuilder() {
    }

    static ObjectNode buildProfileSyncDevice(LenderDeviceContext device) {
        return OBJECT_MAPPER.valueToTree(LenderDevicePayloadBuilder.buildProfileSyncDevice(device));
    }

    static ObjectNode buildRiskApplyDevice(LenderDeviceContext device) {
        return OBJECT_MAPPER.valueToTree(LenderDevicePayloadBuilder.buildRiskApplyDevice(device));
    }
}
