package com.pk.infra.tracking;

import com.pk.core.tracking.port.TrackingEventRepository;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrackingEventRepository implements TrackingEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTrackingEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<String> findExistingEventIds(Collection<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Set.of();
        }
        List<String> idList = new ArrayList<>(eventIds);
        String placeholders = String.join(", ", Collections.nCopies(idList.size(), "?"));
        List<String> existing = jdbcTemplate.query(
                "SELECT event_id FROM tracking_event WHERE event_id IN (" + placeholders + ")",
                (rs, rowNum) -> rs.getString("event_id"),
                idList.toArray()
        );
        return new HashSet<>(existing);
    }

    @Override
    public void insertBatch(Collection<TrackingEventInsert> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                """
                INSERT INTO tracking_event (
                    event_id,
                    trace_id,
                    partner_user_id,
                    profile_id,
                    event_type,
                    event_time,
                    url,
                    device_no,
                    extend_json,
                    source
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                events,
                events.size(),
                (PreparedStatement statement, TrackingEventInsert event) -> {
                    statement.setString(1, event.eventId());
                    statement.setString(2, event.traceId());
                    statement.setString(3, event.partnerUserId());
                    if (event.profileId() == null) {
                        statement.setObject(4, null);
                    } else {
                        statement.setLong(4, event.profileId());
                    }
                    statement.setString(5, event.eventType());
                    statement.setTimestamp(6, Timestamp.from(event.eventTime()));
                    statement.setString(7, event.url());
                    statement.setString(8, event.deviceNo());
                    statement.setString(9, event.extendJson());
                    statement.setString(10, event.source());
                }
        );
    }
}
