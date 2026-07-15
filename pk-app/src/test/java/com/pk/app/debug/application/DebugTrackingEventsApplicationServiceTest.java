package com.pk.app.debug.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.core.api.ApiException;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DebugTrackingEventsApplicationServiceTest {
    @Test
    void returnsEventsGroupedByTypeForClientNo() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        DebugTrackingEventsApplicationService service = new DebugTrackingEventsApplicationService(
                readMapper,
                properties
        );
        when(readMapper.countByClientNo("c4797ba1f703219c")).thenReturn(2L);
        when(readMapper.countEventTypesByClientNo("c4797ba1f703219c"))
                .thenReturn(List.of(new DebugTrackingReadMapper.EventTypeCountRecord("page_view", 2L)));
        when(readMapper.findByClientNo("c4797ba1f703219c", 200))
                .thenReturn(List.of(sampleRecord(1L, "page_view"), sampleRecord(2L, "page_view")));

        var response = service.query("debug-token", "c4797ba1f703219c", null);

        assertThat(response.found()).isTrue();
        assertThat(response.clientNo()).isEqualTo("c4797ba1f703219c");
        assertThat(response.total()).isEqualTo(2L);
        assertThat(response.eventTypes()).hasSize(1);
        assertThat(response.eventTypes().getFirst().eventType()).isEqualTo("page_view");
        assertThat(response.events()).hasSize(2);
        assertThat(response.truncated()).isFalse();
    }

    @Test
    void returnsEmptyWhenNoEvents() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        DebugTrackingEventsApplicationService service = new DebugTrackingEventsApplicationService(
                readMapper,
                properties
        );
        when(readMapper.countByClientNo("missing")).thenReturn(0L);

        var response = service.query("debug-token", "missing", null);

        assertThat(response.found()).isFalse();
        assertThat(response.events()).isEmpty();
    }

    @Test
    void rejectsInvalidDebugToken() {
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        DebugTrackingEventsApplicationService service = new DebugTrackingEventsApplicationService(
                mock(DebugTrackingReadMapper.class),
                properties
        );

        assertThatThrownBy(() -> service.query("bad", "c1", null))
                .isInstanceOf(ApiException.class);
    }

    private static DebugTrackingReadMapper.TrackingEventRecord sampleRecord(long id, String eventType) {
        return new DebugTrackingReadMapper.TrackingEventRecord(
                id,
                1_700_000_000_000L,
                "u1",
                eventType,
                "https://app.example/home",
                "{}",
                "trace-1",
                "c4797ba1f703219c",
                "Xiaomi",
                "Mi 10",
                "phone",
                "Android",
                "13",
                "com.pk.app",
                "1.0.0",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "1.2.3.4",
                "2026-07-15 10:00:00",
                "{\"eventType\":\"" + eventType + "\"}",
                "U10001",
                10L,
                "client",
                Instant.parse("2026-07-15T02:00:00Z")
        );
    }
}
