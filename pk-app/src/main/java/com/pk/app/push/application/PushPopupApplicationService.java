package com.pk.app.push.application;

import com.pk.app.push.dto.response.PopupDisplayedResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.PushDisplayLogRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

/** Display reporting for mandatory in-app popups (audit trail; delivery relies on FCM). */
@Service
public class PushPopupApplicationService {
    private final PushNotificationTaskRepository taskRepository;
    private final PushDisplayLogRepository displayLogRepository;

    public PushPopupApplicationService(
            PushNotificationTaskRepository taskRepository,
            PushDisplayLogRepository displayLogRepository
    ) {
        this.taskRepository = taskRepository;
        this.displayLogRepository = displayLogRepository;
    }

    public PopupDisplayedResponse markDisplayed(AuthenticatedPrincipal principal, long popupId) {
        requirePrincipal(principal);
        PushNotificationTask task = taskRepository.findById(popupId)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "popup not found"));
        if (!task.includesInternal() || !task.requiredRead()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "popup is not a mandatory popup");
        }
        boolean recorded = displayLogRepository.insertDisplayedIfAbsent(
                principal.userId(),
                popupId,
                Instant.now()
        );
        return new PopupDisplayedResponse(Long.toString(popupId), recorded);
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() <= 0) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }
}
