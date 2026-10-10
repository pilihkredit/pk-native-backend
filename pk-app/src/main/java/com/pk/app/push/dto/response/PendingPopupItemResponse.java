package com.pk.app.push.dto.response;

import java.util.List;

/**
 * One pending mandatory popup, shaped exactly like the FCM data contract plus the
 * notification-level fields, so the client parses FCM pushes and pulled popups alike.
 */
public record PendingPopupItemResponse(
        String popupId,
        String type,
        String mandatory,
        String title,
        String body,
        String path,
        List<String> showOn,
        String popupUrl,
        String clickUrl
) {
}
