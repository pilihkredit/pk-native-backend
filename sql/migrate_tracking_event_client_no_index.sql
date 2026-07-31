-- Speed up debug query: GET /debug/tracking-events?clientNo=
ALTER TABLE tracking_event
    ADD KEY idx_tracking_event_client_no (client_no, event_timestamp);
