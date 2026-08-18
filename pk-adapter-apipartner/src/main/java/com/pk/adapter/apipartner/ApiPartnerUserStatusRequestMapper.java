package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.home.port.LenderUserStatusPort;

final class ApiPartnerUserStatusRequestMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ApiPartnerUserStatusRequestMapper() {
    }

    static String buildBody(LenderUserStatusPort.LenderUserStatusCommand command) {
        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("partnerUserId", command.partnerUserId());
        body.set("openUserDevice", ApiPartnerDeviceNodeBuilder.buildProfileSyncDevice(command.device()));
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize lender user status request", exception);
        }
    }
}
