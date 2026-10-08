package com.pk.app.push.dto.response;

import java.util.List;

public record PendingPopupListResponse(List<PendingPopupItemResponse> popups) {
}
