package com.pk.app.tracking.application;

import com.pk.app.tracking.dto.request.TrackingEventRequest;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.tracking.TrackingFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrackingApplicationService {
    private static final Logger log = LoggerFactory.getLogger(TrackingApplicationService.class);

    private final TrackingFacade trackingFacade;

    public TrackingApplicationService(TrackingFacade trackingFacade) {
        this.trackingFacade = trackingFacade;
    }

    public void ingest(
            AuthenticatedPrincipal principal,
            boolean bearerTokenPresent,
            String clientIp,
            TrackingEventRequest request
    ) {
        ResolvedIdentity identity = resolveIdentity(principal, bearerTokenPresent, request);
        trackingFacade.ingest(
                identity.userId(),
                identity.partnerUserId(),
                clientIp,
                new TrackingFacade.TrackingEventCommand(
                        request.timestamp(),
                        request.uid(),
                        request.eventType(),
                        request.url(),
                        request.extend(),
                        request.traceId(),
                        request.clientNo(),
                        request.clientManufacture(),
                        request.clientModel(),
                        request.clientCategory(),
                        request.clientOs(),
                        request.clientOsVersion(),
                        request.ai(),
                        request.av(),
                        request.wv(),
                        request.bn(),
                        request.bv(),
                        request.androidId(),
                        request.gaid(),
                        request.idfv(),
                        request.idfa()
                )
        );
    }

    /**
     * Anonymous tracking is allowed. When the caller is logged in, userId / partnerUserId
     * should always come from the JWT principal; log explicitly if either is missing.
     */
    static ResolvedIdentity resolveIdentity(
            AuthenticatedPrincipal principal,
            boolean bearerTokenPresent,
            TrackingEventRequest request
    ) {
        if (principal == null) {
            if (bearerTokenPresent) {
                log.warn(
                        "tracking event without login identity: Authorization present but principal missing "
                                + "(token invalid/expired/rejected on public API). eventType={} traceId={} clientNo={}",
                        request == null ? null : request.eventType(),
                        request == null ? null : request.traceId(),
                        request == null ? null : request.clientNo()
                );
            }
            return new ResolvedIdentity(null, null);
        }

        Long userId = null;
        if (principal.userId() <= 0) {
            log.warn(
                    "tracking event logged-in but userId invalid: userId={} partnerUserId={} eventType={} traceId={}",
                    principal.userId(),
                    principal.partnerUserId(),
                    request == null ? null : request.eventType(),
                    request == null ? null : request.traceId()
            );
        } else {
            userId = principal.userId();
        }

        String partnerUserId = null;
        if (principal.partnerUserId() == null || principal.partnerUserId().isBlank()) {
            log.warn(
                    "tracking event logged-in but partnerUserId blank: userId={} eventType={} traceId={}",
                    principal.userId(),
                    request == null ? null : request.eventType(),
                    request == null ? null : request.traceId()
            );
        } else {
            partnerUserId = principal.partnerUserId().trim();
        }

        return new ResolvedIdentity(userId, partnerUserId);
    }

    record ResolvedIdentity(Long userId, String partnerUserId) {
    }
}
