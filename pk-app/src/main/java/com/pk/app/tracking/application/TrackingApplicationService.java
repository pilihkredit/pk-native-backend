package com.pk.app.tracking.application;

import com.pk.app.tracking.dto.request.TrackingEventsRequest;
import com.pk.app.tracking.dto.response.TrackingEventsResponse;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.tracking.TrackingFacade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrackingApplicationService {
    private final TrackingFacade trackingFacade;

    public TrackingApplicationService(TrackingFacade trackingFacade) {
        this.trackingFacade = trackingFacade;
    }

    public TrackingEventsResponse ingest(
            AuthenticatedPrincipal principal,
            String deviceNo,
            String clientIp,
            TrackingEventsRequest request
    ) {
        Long profileId = principal == null ? null : principal.profileId();
        String partnerUserId = principal == null ? null : principal.partnerUserId();
        List<TrackingFacade.TrackingEventCommand> commands = request.events().stream()
                .map(item -> new TrackingFacade.TrackingEventCommand(
                        item.eventId(),
                        item.eventType(),
                        item.eventTime(),
                        item.traceId(),
                        item.url(),
                        item.extend(),
                        item.clientNo(),
                        item.clientManufacture(),
                        item.clientModel(),
                        item.clientCategory(),
                        item.clientOs(),
                        item.clientOsVersion(),
                        item.ai(),
                        item.av(),
                        item.wv(),
                        item.bn(),
                        item.bv(),
                        item.androidId(),
                        item.gaid(),
                        item.idfv(),
                        item.idfa()
                ))
                .toList();
        TrackingFacade.IngestResult result = trackingFacade.ingest(
                profileId,
                partnerUserId,
                deviceNo,
                clientIp,
                commands
        );
        return new TrackingEventsResponse(result.acceptedCount(), result.rejectedCount());
    }
}
