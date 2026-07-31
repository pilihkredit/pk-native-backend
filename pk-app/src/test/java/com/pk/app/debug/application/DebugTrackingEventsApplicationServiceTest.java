package com.pk.app.debug.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper.TrackingQueryCriteria;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DebugTrackingEventsApplicationServiceTest {
    @Test
    void returnsEventsGroupedByTypeForClientNo() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        DebugTrackingEventsApplicationService service = newService(readMapper, userAuthRepository);
        when(readMapper.countByCriteria(any())).thenReturn(2L);
        when(readMapper.countEventTypesByCriteria(any()))
                .thenReturn(List.of(new DebugTrackingReadMapper.EventTypeCountRecord("page_view", 2L)));
        when(readMapper.findByCriteria(any(), eq(200)))
                .thenReturn(List.of(sampleRecord(1L, "page_view"), sampleRecord(2L, "page_view")));

        var response = service.query("debug-token", "c4797ba1f703219c", null, null, null);

        assertThat(response.found()).isTrue();
        assertThat(response.clientNo()).isEqualTo("c4797ba1f703219c");
        assertThat(response.total()).isEqualTo(2L);
        assertThat(response.eventTypes()).hasSize(1);
        assertThat(response.events()).hasSize(2);
        assertThat(response.truncated()).isFalse();
    }

    @Test
    void queriesByMobileNoViaProfileIdentity() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        DebugTrackingEventsApplicationService service = newService(readMapper, userAuthRepository);
        when(userAuthRepository.findByMobileNo("801234567"))
                .thenReturn(Optional.of(new UserProfileSummary(10L, "U10001", "801234567", false)));
        ArgumentCaptor<TrackingQueryCriteria> criteriaCaptor = ArgumentCaptor.forClass(TrackingQueryCriteria.class);
        when(readMapper.countByCriteria(criteriaCaptor.capture())).thenReturn(1L);
        when(readMapper.countEventTypesByCriteria(any()))
                .thenReturn(List.of(new DebugTrackingReadMapper.EventTypeCountRecord("login", 1L)));
        when(readMapper.findByCriteria(any(), eq(200)))
                .thenReturn(List.of(sampleRecord(3L, "login")));

        var response = service.query("debug-token", null, "801234567", null, null);

        assertThat(response.found()).isTrue();
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.partnerUserId()).isEqualTo("U10001");
        TrackingQueryCriteria criteria = criteriaCaptor.getValue();
        assertThat(criteria.userId()).isEqualTo(10L);
        assertThat(criteria.userIds()).containsExactly("U10001");
    }

    @Test
    void queriesByPartnerUserId() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        DebugTrackingEventsApplicationService service = newService(readMapper, userAuthRepository);
        ArgumentCaptor<TrackingQueryCriteria> criteriaCaptor = ArgumentCaptor.forClass(TrackingQueryCriteria.class);
        when(readMapper.countByCriteria(criteriaCaptor.capture())).thenReturn(1L);
        when(readMapper.countEventTypesByCriteria(any()))
                .thenReturn(List.of(new DebugTrackingReadMapper.EventTypeCountRecord("click", 1L)));
        when(readMapper.findByCriteria(any(), eq(50)))
                .thenReturn(List.of(sampleRecord(4L, "click")));

        var response = service.query("debug-token", null, null, "U10001", 50);

        assertThat(response.found()).isTrue();
        assertThat(response.requestedUserId()).isEqualTo("U10001");
        assertThat(criteriaCaptor.getValue().userIds()).containsExactly("U10001");
        assertThat(criteriaCaptor.getValue().userId()).isNull();
    }

    @Test
    void returnsEmptyWhenMobileNotFound() {
        DebugTrackingReadMapper readMapper = mock(DebugTrackingReadMapper.class);
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        DebugTrackingEventsApplicationService service = newService(readMapper, userAuthRepository);
        when(userAuthRepository.findByMobileNo("809999999")).thenReturn(Optional.empty());

        var response = service.query("debug-token", null, "809999999", null, null);

        assertThat(response.found()).isFalse();
        assertThat(response.events()).isEmpty();
    }

    @Test
    void rejectsWhenNoQueryKeyProvided() {
        DebugTrackingEventsApplicationService service = newService(
                mock(DebugTrackingReadMapper.class),
                mock(UserAuthRepository.class)
        );

        assertThatThrownBy(() -> service.query("debug-token", " ", null, "", null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsInvalidDebugToken() {
        DebugTrackingEventsApplicationService service = newService(
                mock(DebugTrackingReadMapper.class),
                mock(UserAuthRepository.class)
        );

        assertThatThrownBy(() -> service.query("bad", "c1", null, null, null))
                .isInstanceOf(ApiException.class);
    }

    private static DebugTrackingEventsApplicationService newService(
            DebugTrackingReadMapper readMapper,
            UserAuthRepository userAuthRepository
    ) {
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        return new DebugTrackingEventsApplicationService(readMapper, userAuthRepository, properties);
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
                "CLIENT",
                Instant.parse("2026-07-15T02:00:00Z")
        );
    }
}
