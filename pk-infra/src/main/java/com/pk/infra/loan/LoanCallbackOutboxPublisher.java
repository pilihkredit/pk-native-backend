package com.pk.infra.loan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import java.util.UUID;

public class LoanCallbackOutboxPublisher {
    private static final String AGGREGATE_TYPE = "callback_event";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public LoanCallbackOutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(long callbackEventId, String loanApplyId) {
        outboxEventRepository.insertPending(new OutboxEventRepository.OutboxEventDraft(
                UUID.randomUUID().toString(),
                OutboxEventTypes.LOAN_CALLBACK,
                AGGREGATE_TYPE,
                Long.toString(callbackEventId),
                serialize(callbackEventId, loanApplyId)
        ));
    }

    public LoanCallbackJob deserialize(String payloadJson) {
        try {
            OutboxPayload payload = objectMapper.readValue(payloadJson, OutboxPayload.class);
            return new LoanCallbackJob(payload.callbackEventId(), payload.loanApplyId());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize loan callback outbox payload", exception);
        }
    }

    private String serialize(long callbackEventId, String loanApplyId) {
        try {
            return objectMapper.writeValueAsString(new OutboxPayload(callbackEventId, loanApplyId));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize loan callback outbox payload", exception);
        }
    }

    private record OutboxPayload(long callbackEventId, String loanApplyId) {
    }
}
