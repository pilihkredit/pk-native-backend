package com.pk.app.tracking.application;

import com.pk.app.tracking.dto.request.TrackingEventRequest;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.tracking.TrackingFacade;
import org.springframework.stereotype.Service;

@Service
public class TrackingApplicationService {
    private final TrackingFacade trackingFacade;

    public TrackingApplicationService(TrackingFacade trackingFacade) {
        this.trackingFacade = trackingFacade;
    }

    public void ingest(
            AuthenticatedPrincipal principal,
            String clientIp,
            TrackingEventRequest request
    ) {
        Long profileId = principal == null ? null : principal.profileId();
        String partnerUserId = principal == null ? null : principal.partnerUserId();
        trackingFacade.ingest(
                profileId,
                partnerUserId,
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
}
