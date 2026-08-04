package com.pk.app.push.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.push.dto.request.PushDeviceRegisterRequest;
import com.pk.app.push.dto.response.InboxMessagePageResponse;
import com.pk.app.push.dto.response.InboxMessageReadResponse;
import com.pk.app.push.dto.response.InboxMessageResponse;
import com.pk.app.push.dto.response.PushDeviceRegisterResponse;
import com.pk.app.push.dto.response.UnreadMessageCountResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.push.InboxMessage;
import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.PushPermissionStatus;
import com.pk.infra.push.InboxMessageFacade;
import com.pk.infra.push.PushDeviceFacade;

public class PushApplicationService {
    private final PushDeviceFacade pushDeviceFacade;
    private final InboxMessageFacade inboxMessageFacade;

    public PushApplicationService(PushDeviceFacade pushDeviceFacade, InboxMessageFacade inboxMessageFacade) {
        this.pushDeviceFacade = pushDeviceFacade;
        this.inboxMessageFacade = inboxMessageFacade;
    }

    public PushDeviceRegisterResponse registerDevice(
            AuthenticatedPrincipal principal,
            PushDeviceRegisterRequest request,
            ClientRequestHeaders.ResolvedClientHeaders headers
    ) {
        Long userId = principal == null ? null : principal.userId();
        pushDeviceFacade.register(new PushDeviceRegistration(
                userId,
                headers.deviceNo(),
                headers.appVersion(),
                headers.platform(),
                headers.appPackage(),
                request.fcmToken(),
                parsePermissionStatus(request.permissionStatus())
        ));
        return new PushDeviceRegisterResponse(true);
    }

    public UnreadMessageCountResponse unreadCount(AuthenticatedPrincipal principal) {
        return new UnreadMessageCountResponse(inboxMessageFacade.unreadCount(requirePrincipal(principal).userId()));
    }

    public InboxMessagePageResponse list(AuthenticatedPrincipal principal, Long cursor, int pageSize) {
        var page = inboxMessageFacade.findPage(requirePrincipal(principal).userId(), cursor, pageSize);
        return new InboxMessagePageResponse(
                page.items().stream().map(this::toResponse).toList(),
                page.nextCursor(),
                page.unreadCount()
        );
    }

    public InboxMessageResponse detail(AuthenticatedPrincipal principal, long messageId) {
        return toResponse(inboxMessageFacade.findDetail(requirePrincipal(principal).userId(), messageId));
    }

    public InboxMessageReadResponse markRead(AuthenticatedPrincipal principal, long messageId) {
        inboxMessageFacade.markRead(requirePrincipal(principal).userId(), messageId);
        return new InboxMessageReadResponse(messageId, true);
    }

    private InboxMessageResponse toResponse(InboxMessage message) {
        return new InboxMessageResponse(
                message.messageId(),
                message.type(),
                message.title(),
                message.summary(),
                message.content(),
                message.read(),
                message.sentAt().toEpochMilli(),
                message.deeplink()
        );
    }

    private static AuthenticatedPrincipal requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return principal;
    }

    private static PushPermissionStatus parsePermissionStatus(String value) {
        try {
            return PushPermissionStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
