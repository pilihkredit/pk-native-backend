package com.pk.infra.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.UUID;

public class ProfileSyncOutboxPublisher {
    private static final String AGGREGATE_TYPE = "user_profile";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ProfileSyncOutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(ProfileSyncJob job) {
        outboxEventRepository.insertPending(new OutboxEventRepository.OutboxEventDraft(
                UUID.randomUUID().toString(),
                OutboxEventTypes.PROFILE_SYNC,
                AGGREGATE_TYPE,
                Long.toString(job.userId()),
                serialize(job)
        ));
    }

    public ProfileSyncJob deserialize(String payloadJson) {
        try {
            OutboxPayload payload = objectMapper.readValue(payloadJson, OutboxPayload.class);
            return new ProfileSyncJob(
                    payload.userId(),
                    payload.partnerUserId(),
                    payload.mobileNo(),
                    payload.requestId(),
                    ProfileSyncModule.valueOf(payload.module()),
                    payload.device(),
                    null
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize profile sync outbox payload", exception);
        }
    }

    private String serialize(ProfileSyncJob job) {
        try {
            return objectMapper.writeValueAsString(new OutboxPayload(
                    job.userId(),
                    job.partnerUserId(),
                    job.mobileNo(),
                    job.requestId(),
                    job.module().name(),
                    job.device()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize profile sync outbox payload", exception);
        }
    }

    private record OutboxPayload(
            long userId,
            String partnerUserId,
            String mobileNo,
            String requestId,
            String module,
            LenderDeviceContext device
    ) {
    }
}
