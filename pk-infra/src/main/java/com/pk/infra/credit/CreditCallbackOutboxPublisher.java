package com.pk.infra.credit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import java.util.UUID;

public class CreditCallbackOutboxPublisher {
    private static final String AGGREGATE_TYPE = "callback_event";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CreditCallbackOutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(long callbackEventId, String applyId) {
        outboxEventRepository.insertPending(new OutboxEventRepository.OutboxEventDraft(
                UUID.randomUUID().toString(),
                OutboxEventTypes.CREDIT_CALLBACK,
                AGGREGATE_TYPE,
                Long.toString(callbackEventId),
                serialize(callbackEventId, applyId)
        ));
    }

    public CreditCallbackJob deserialize(String payloadJson) {
        try {
            OutboxPayload payload = objectMapper.readValue(payloadJson, OutboxPayload.class);
            return new CreditCallbackJob(payload.callbackEventId(), payload.applyId());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize credit callback outbox payload", exception);
        }
    }

    private String serialize(long callbackEventId, String applyId) {
        try {
            return objectMapper.writeValueAsString(new OutboxPayload(callbackEventId, applyId));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize credit callback outbox payload", exception);
        }
    }

    private record OutboxPayload(long callbackEventId, String applyId) {
    }
}
