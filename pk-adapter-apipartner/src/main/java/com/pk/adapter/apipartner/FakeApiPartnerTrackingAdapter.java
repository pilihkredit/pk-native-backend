package com.pk.adapter.apipartner;

import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import java.util.Collection;

public class FakeApiPartnerTrackingAdapter implements LenderTrackingPort {
    @Override
    public void submitEvents(Collection<LenderTrackingEvent> events) {
    }
}
