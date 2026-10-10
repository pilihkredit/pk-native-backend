package com.pk.infra.push;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.PushNotificationTask;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the client-facing FCM payload contract:
 *
 * <pre>
 * data.type        IN_APP_POPUP / EXTERNAL_NOTIFY / IN_APP_POPUP_CANCEL
 * data.popupId     push_task.id as string
 * data.mandatory   "true" / "false" (in-app popup only)
 * data.path        H5 preset popup path with title/body/button/showOn/bannerUrl/url query
 * data.clickUrl    system-notification tap target (external/all)
 * data.popupIds    comma list of push_task ids (clear message only)
 * </pre>
 */
public class PushPayloadAssembler {
    public static final String TYPE_IN_APP_POPUP = "IN_APP_POPUP";
    public static final String TYPE_EXTERNAL_NOTIFY = "EXTERNAL_NOTIFY";
    public static final String TYPE_IN_APP_POPUP_CANCEL = "IN_APP_POPUP_CANCEL";

    public static final String DEFAULT_POPUP_PATH = "/in-app-popup";
    private static final int FCM_DATA_MAX_BYTES = 4096;

    /** Notification title/body plus the shared data map, reused for every recipient token. */
    public record FcmTemplate(String notificationTitle, String notificationBody, Map<String, String> data) {
    }

    public FcmTemplate assemble(PushNotificationTask task) {
        if (task == null || task.pushType() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "push task is invalid");
        }
        Map<String, String> data = new LinkedHashMap<>();
        String popupId = Long.toString(task.id());
        data.put("popupId", popupId);
        if (task.isClearRequired()) {
            data.put("type", TYPE_IN_APP_POPUP_CANCEL);
            data.put("popupIds", task.clearTargetIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            assertDataSize(data);
            return new FcmTemplate(null, null, data);
        }
        if (task.includesInternal()) {
            data.put("type", TYPE_IN_APP_POPUP);
            data.put("mandatory", Boolean.toString(task.requiredRead()));
            data.put("path", buildPopupPath(task));
            // Client contract: the popup button jump target is a standalone data field;
            // absent means the button only closes the popup.
            putIfNotBlank(data, "popupUrl", task.targetUrl());
            if (task.includesExternal()) {
                putIfNotBlank(data, "clickUrl", task.externalUrl());
            }
            assertDataSize(data);
            // Notification part only when the system tray may show it (external/all);
            // internal-only pushes must stay data-only so backgrounded devices never
            // auto-display a system notification.
            if (task.includesExternal()) {
                return new FcmTemplate(task.title(), task.body(), data);
            }
            return new FcmTemplate(null, null, data);
        }
        data.put("type", TYPE_EXTERNAL_NOTIFY);
        putIfNotBlank(data, "clickUrl", task.externalUrl());
        assertDataSize(data);
        return new FcmTemplate(task.title(), task.body(), data);
    }

    /**
     * Popup path handed to the H5 preset popup page; also reused by the pending pull API.
     * The prefix is fixed to {@value #DEFAULT_POPUP_PATH} per the client contract
     * ("端内URL" backoffice input is obsolete); title/body/button/showOn/bannerUrl ride
     * the query, the jump target lives in data.popupUrl instead.
     */
    public String buildPopupPath(PushNotificationTask task) {
        StringBuilder path = new StringBuilder(DEFAULT_POPUP_PATH);
        StringBuilder query = new StringBuilder();
        appendQuery(query, "title", task.title());
        appendQuery(query, "body", task.body());
        appendQuery(query, "button", task.buttonText());
        if (task.pushPages() != null && !task.pushPages().isEmpty()) {
            appendQuery(query, "showOn", String.join(",", task.pushPages()));
        }
        appendQuery(query, "bannerUrl", task.bannerUrl());
        if (query.length() > 0) {
            path.append('?').append(query);
        }
        return path.toString();
    }

    private static void appendQuery(StringBuilder query, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (query.length() > 0) {
            query.append('&');
        }
        query.append(name).append('=')
                .append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8));
    }

    private static void putIfNotBlank(Map<String, String> data, String key, String value) {
        if (value != null && !value.isBlank()) {
            data.put(key, value.trim());
        }
    }

    private static void assertDataSize(Map<String, String> data) {
        int bytes = data.entrySet().stream()
                .mapToInt(entry -> (entry.getKey().length() + entry.getValue().length()
                        + "type".length()) * 2)
                .sum();
        if (bytes > FCM_DATA_MAX_BYTES) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "push payload exceeds FCM data limit");
        }
    }
}
