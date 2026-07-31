package com.pk.adapter.pendanaan;

import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import java.util.Collection;

public class FakePendanaanTrackingAdapter implements LenderTrackingPort {
    @Override
    public void submitEvents(Collection<LenderTrackingEvent> events) {
    }
}
