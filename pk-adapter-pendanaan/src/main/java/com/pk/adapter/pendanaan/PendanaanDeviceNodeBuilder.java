package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.Map;

final class PendanaanDeviceNodeBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PendanaanDeviceNodeBuilder() {
    }

    static ObjectNode buildProfileSyncDevice(LenderDeviceContext device) {
        return build(device, false);
    }

    static ObjectNode buildRiskApplyDevice(LenderDeviceContext device) {
        return build(device, true);
    }

    private static ObjectNode build(LenderDeviceContext device, boolean requireDeviceOtherInfo) {
        ObjectNode deviceNode = OBJECT_MAPPER.createObjectNode();
        deviceNode.put("appName", device.appName());
        deviceNode.put("appVersion", device.appVersion());
        deviceNode.put("packageName", device.packageName());
        deviceNode.put("deviceNo", device.deviceNo());
        deviceNode.put("systemPlatform", device.systemPlatform());
        applyExtendedAttributes(deviceNode, device.resolvedExtendedAttributes());
        putIfPresent(deviceNode, "adId", device.adId());
        applyDeviceOtherInfo(deviceNode, device.deviceOtherInfo(), requireDeviceOtherInfo);
        return deviceNode;
    }

    private static void applyExtendedAttributes(ObjectNode deviceNode, DeviceExtendedAttributes attributes) {
        putIfPresent(deviceNode, "phoneBrand", attributes.phoneBrand());
        putIfPresent(deviceNode, "phoneBrandModel", attributes.phoneBrandModel());
        putIfPresent(deviceNode, "mac", attributes.mac());
        putIfPresent(deviceNode, "systemVersion", attributes.systemVersion());
        putIfPresent(deviceNode, "deliveryPlatform", attributes.deliveryPlatform());
        if (attributes.cpuCores() != null) {
            deviceNode.put("cpuCores", attributes.cpuCores());
        }
        if (attributes.memoryTotal() != null) {
            deviceNode.put("memoryTotal", attributes.memoryTotal());
        }
        if (attributes.sdCardTotal() != null) {
            deviceNode.put("sdCardTotal", attributes.sdCardTotal());
        }
        putIfPresent(deviceNode, "idfv", attributes.idfv());
        putIfPresent(deviceNode, "idfa", attributes.idfa());
        putIfPresent(deviceNode, "extParam", attributes.extParam());
    }

    private static void applyDeviceOtherInfo(
            ObjectNode deviceNode,
            Map<String, Object> deviceOtherInfo,
            boolean required
    ) {
        if (required) {
            if (deviceOtherInfo == null || deviceOtherInfo.isEmpty()) {
                deviceNode.putObject("deviceOtherInfo");
            } else {
                deviceNode.set("deviceOtherInfo", OBJECT_MAPPER.valueToTree(deviceOtherInfo));
            }
            return;
        }
        if (deviceOtherInfo != null && !deviceOtherInfo.isEmpty()) {
            deviceNode.set("deviceOtherInfo", OBJECT_MAPPER.valueToTree(deviceOtherInfo));
        }
    }

    private static void putIfPresent(ObjectNode node, String field, String value) {
        if (value != null && !value.isBlank()) {
            node.put(field, value.trim());
        }
    }
}
