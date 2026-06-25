package com.pk.infra.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.tracking.port.TrackingEventRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrackingFacadeTest {
    private InMemoryTrackingEventRepository repository;
    private TrackingFacade facade;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrackingEventRepository();
        facade = new TrackingFacade(repository, new ObjectMapper());
    }

    @Test
    void acceptsValidEventsAndPersistsBatch() {
        TrackingFacade.IngestResult result = facade.ingest(
                10L,
                "partner-1",
                "device-1",
                List.of(validEvent("EVT-1"), validEvent("EVT-2"))
        );

        assertThat(result.acceptedCount()).isEqualTo(2);
        assertThat(result.rejectedCount()).isEqualTo(0);
        assertThat(repository.inserted).hasSize(2);
    }

    @Test
    void dedupesWithinBatchAndAgainstDatabase() {
        repository.existingIds.add("EVT-EXISTING");

        TrackingFacade.IngestResult result = facade.ingest(
                10L,
                "partner-1",
                "device-1",
                List.of(
                        validEvent("EVT-1"),
                        validEvent("EVT-1"),
                        validEvent("EVT-EXISTING"),
                        invalidEvent()
                )
        );

        assertThat(result.acceptedCount()).isEqualTo(1);
        assertThat(result.rejectedCount()).isEqualTo(3);
        assertThat(repository.inserted).hasSize(1);
        assertThat(repository.inserted.getFirst().eventId()).isEqualTo("EVT-1");
    }

    @Test
    void rejectsEmptyOrOversizedBatch() {
        assertThatThrownBy(() -> facade.ingest(1L, "partner", "device", List.of()))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);

        List<TrackingFacade.TrackingEventCommand> tooMany = java.util.stream.IntStream.rangeClosed(1, 51)
                .mapToObj(i -> validEvent("EVT-" + i))
                .toList();
        assertThatThrownBy(() -> facade.ingest(1L, "partner", "device", tooMany))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void returnsZeroAcceptedWhenAllEventsInvalid() {
        TrackingFacade.IngestResult result = facade.ingest(
                10L,
                "partner-1",
                "device-1",
                List.of(invalidEvent())
        );

        assertThat(result.acceptedCount()).isEqualTo(0);
        assertThat(result.rejectedCount()).isEqualTo(1);
        assertThat(repository.inserted).isEmpty();
    }

    private static TrackingFacade.TrackingEventCommand validEvent(String eventId) {
        return new TrackingFacade.TrackingEventCommand(
                eventId,
                "loan_page_enter",
                1_750_500_000_000L,
                "trace-1",
                "/loan",
                Map.of("applyId", "APPLY-1")
        );
    }

    private static TrackingFacade.TrackingEventCommand invalidEvent() {
        return new TrackingFacade.TrackingEventCommand(
                "",
                "loan_page_enter",
                1_750_500_000_000L,
                "trace-1",
                "/loan",
                null
        );
    }

    private static final class InMemoryTrackingEventRepository implements TrackingEventRepository {
        private final Set<String> existingIds = new HashSet<>();
        private final List<TrackingEventInsert> inserted = new ArrayList<>();

        @Override
        public Set<String> findExistingEventIds(Collection<String> eventIds) {
            Set<String> found = new HashSet<>();
            for (String eventId : eventIds) {
                if (existingIds.contains(eventId)) {
                    found.add(eventId);
                }
            }
            return found;
        }

        @Override
        public void insertBatch(Collection<TrackingEventInsert> events) {
            for (TrackingEventInsert event : events) {
                inserted.add(event);
                existingIds.add(event.eventId());
            }
        }
    }
}
