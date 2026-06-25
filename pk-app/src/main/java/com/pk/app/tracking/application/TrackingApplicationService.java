package com.pk.app.tracking.application;

import com.pk.app.tracking.dto.request.TrackingEventsRequest;
import com.pk.app.tracking.dto.response.TrackingEventsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
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
            TrackingEventsRequest request
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        List<TrackingFacade.TrackingEventCommand> commands = request.events().stream()
                .map(item -> new TrackingFacade.TrackingEventCommand(
                        item.eventId(),
                        item.eventType(),
                        item.eventTime(),
                        item.traceId(),
                        item.url(),
                        item.extend()
                ))
                .toList();
        TrackingFacade.IngestResult result = trackingFacade.ingest(
                principal.profileId(),
                principal.partnerUserId(),
                deviceNo,
                commands
        );
        return new TrackingEventsResponse(result.acceptedCount(), result.rejectedCount());
    }
}
