package com.pk.infra.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import com.pk.core.tracking.port.TrackingEventRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrackingFacadeTest {
    private InMemoryTrackingEventRepository repository;
    private RecordingLenderTrackingPort lenderTrackingPort;
    private TrackingFacade facade;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrackingEventRepository();
        lenderTrackingPort = new RecordingLenderTrackingPort();
        facade = new TrackingFacade(repository, lenderTrackingPort, objectMapper);
    }

    @Test
    void acceptsValidEventPersistsAndForwardsToLender() throws Exception {
        facade.ingest(10L, "partner-1", "192.168.1.10", validEvent(null));

        assertThat(lenderTrackingPort.submitted).hasSize(1);
        LenderTrackingEvent forwarded = lenderTrackingPort.submitted.getFirst();
        assertThat(forwarded.uid()).isEqualTo("partner-1");
        assertThat(forwarded.clientNo()).isEqualTo("device-1");
        assertThat(forwarded.ip()).isEqualTo("192.168.1.10");
        assertThat(forwarded.datetime()).isEqualTo("2025-06-21 17:00:00");
        assertThat(forwarded.timestamp()).isEqualTo(1_750_500_000_000L);

        assertThat(repository.inserted).hasSize(1);
        TrackingEventRepository.TrackingEventInsert saved = repository.inserted.getFirst();
        assertThat(saved.eventTimestamp()).isEqualTo(1_750_500_000_000L);
        assertThat(saved.uid()).isEqualTo("partner-1");
        assertThat(saved.eventType()).isEqualTo("loan_page_enter");
        assertThat(saved.clientManufacture()).isEqualTo("Apple");
        assertThat(saved.ip()).isEqualTo("192.168.1.10");
        assertThat(saved.eventDatetime()).isEqualTo("2025-06-21 17:00:00");
        assertThat(saved.partnerUserId()).isEqualTo("partner-1");
        assertThat(saved.profileId()).isEqualTo(10L);
        assertThat(saved.source()).isEqualTo("CLIENT");

        JsonNode payload = objectMapper.readTree(saved.payloadJson());
        assertThat(payload.get("timestamp").asLong()).isEqualTo(1_750_500_000_000L);
        assertThat(payload.get("uid").asText()).isEqualTo("partner-1");
        assertThat(payload.get("eventType").asText()).isEqualTo("loan_page_enter");
        assertThat(payload.get("clientNo").asText()).isEqualTo("device-1");
        assertThat(payload.get("ip").asText()).isEqualTo("192.168.1.10");
        assertThat(payload.get("datetime").asText()).isEqualTo("2025-06-21 17:00:00");
        assertThat(payload.get("extend").get("applyId").asText()).isEqualTo("APPLY-1");
    }

    @Test
    void prefersRequestUidOverPartnerUserId() {
        facade.ingest(10L, "partner-1", "192.168.1.10", validEvent("uid-from-client"));

        assertThat(lenderTrackingPort.submitted.getFirst().uid()).isEqualTo("uid-from-client");
        assertThat(repository.inserted.getFirst().uid()).isEqualTo("uid-from-client");
    }

    @Test
    void acceptsEventsWithoutAuthenticatedUser() {
        facade.ingest(null, null, "192.168.1.10", validEvent(""));

        assertThat(lenderTrackingPort.submitted.getFirst().uid()).isEmpty();
        assertThat(repository.inserted.getFirst().profileId()).isNull();
        assertThat(repository.inserted.getFirst().partnerUserId()).isNull();
    }

    @Test
    void rejectsInvalidEvent() {
        assertThatThrownBy(() -> facade.ingest(1L, "partner", "1.1.1.1", invalidEvent()))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);

        assertThat(lenderTrackingPort.submitted).isEmpty();
        assertThat(repository.inserted).isEmpty();
    }

    @Test
    void doesNotPersistWhenLenderRejectsEvent() {
        lenderTrackingPort.fail = true;

        assertThatThrownBy(() -> facade.ingest(10L, "partner-1", "192.168.1.10", validEvent(null)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);

        assertThat(lenderTrackingPort.submitted).hasSize(1);
        assertThat(repository.inserted).isEmpty();
    }

    private static TrackingFacade.TrackingEventCommand validEvent(String uid) {
        return new TrackingFacade.TrackingEventCommand(
                1_750_500_000_000L,
                uid,
                "loan_page_enter",
                "/loan",
                Map.of("applyId", "APPLY-1"),
                "trace-1",
                "device-1",
                "Apple",
                "iPhone 7",
                "phone",
                "iOS",
                "9.2.1",
                "com.pk.app",
                "1.0.0",
                "1.0.0",
                "Chrome",
                "126.0.0",
                null,
                null,
                "idfv-1",
                "idfa-1"
        );
    }

    private static TrackingFacade.TrackingEventCommand invalidEvent() {
        return new TrackingFacade.TrackingEventCommand(
                1_750_500_000_000L,
                null,
                "loan_page_enter",
                "",
                null,
                "trace-1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static final class InMemoryTrackingEventRepository implements TrackingEventRepository {
        private final List<TrackingEventInsert> inserted = new ArrayList<>();

        @Override
        public void insert(TrackingEventInsert event) {
            inserted.add(event);
        }
    }

    private static final class RecordingLenderTrackingPort implements LenderTrackingPort {
        private final List<LenderTrackingEvent> submitted = new ArrayList<>();
        private boolean fail;

        @Override
        public void submitEvents(Collection<LenderTrackingEvent> events) {
            submitted.addAll(events);
            if (fail) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
        }
    }
}
