package com.pk.core.tracking.port;

import com.pk.core.tracking.LenderTrackingEvent;
import java.util.Collection;

public interface LenderTrackingPort {
    void submitEvents(Collection<LenderTrackingEvent> events);
}
