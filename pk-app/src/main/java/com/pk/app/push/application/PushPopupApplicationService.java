package com.pk.app.push.application;

import com.pk.app.push.dto.response.PendingPopupItemResponse;
import com.pk.app.push.dto.response.PopupDisplayedResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.push.PushNotificationTask;
import com.pk.core.push.port.PushDisplayLogRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import com.pk.infra.push.PushPayloadAssembler;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Pull fallback and display reporting for mandatory in-app popups: the client calls this
 * on cold start and foreground resume, and reports each successful popup display.
 */
@Service
public class PushPopupApplicationService {
    private static final int PENDING_LIMIT = 20;

    private final PushNotificationTaskRepository taskRepository;
    private final PushDisplayLogRepository displayLogRepository;
    private final PushPayloadAssembler payloadAssembler;

    public PushPopupApplicationService(
            PushNotificationTaskRepository taskRepository,
            PushDisplayLogRepository displayLogRepository,
            PushPayloadAssembler payloadAssembler
    ) {
        this.taskRepository = taskRepository;
        this.displayLogRepository = displayLogRepository;
        this.payloadAssembler = payloadAssembler;
    }

    public List<PendingPopupItemResponse> pendingPopups(AuthenticatedPrincipal principal) {
        requirePrincipal(principal);
        return taskRepository.findPendingRequiredForUser(principal.userId(), PENDING_LIMIT).stream()
                .map(this::toItem)
                .toList();
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

    private PendingPopupItemResponse toItem(PushNotificationTask task) {
        return new PendingPopupItemResponse(
                Long.toString(task.id()),
                PushPayloadAssembler.TYPE_IN_APP_POPUP,
                Boolean.toString(task.requiredRead()),
                task.title(),
                task.body(),
                payloadAssembler.buildPopupPath(task),
                task.pushPages(),
                blankToNull(task.externalUrl())
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() <= 0) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }
}
