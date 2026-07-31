package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.home.port.LenderUserStatusPort;

final class PendanaanUserStatusRequestMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PendanaanUserStatusRequestMapper() {
    }

    static String buildBody(LenderUserStatusPort.LenderUserStatusCommand command) {
        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("partnerUserId", command.partnerUserId());
        body.set("openUserDevice", PendanaanDeviceNodeBuilder.buildProfileSyncDevice(command.device()));
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize lender user status request", exception);
        }
    }
}
