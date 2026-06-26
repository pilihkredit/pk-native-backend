package com.pk.infra.credit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CreditApplyOutboxPublisher {
    private static final String AGGREGATE_TYPE = "credit_application";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CreditApplyOutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(CreditApplyJob job) {
        outboxEventRepository.insertPending(new OutboxEventRepository.OutboxEventDraft(
                UUID.randomUUID().toString(),
                OutboxEventTypes.CREDIT_APPLY,
                AGGREGATE_TYPE,
                job.applyId(),
                serialize(job)
        ));
    }

    public CreditApplyJob deserialize(String payloadJson) {
        try {
            OutboxPayload payload = objectMapper.readValue(payloadJson, OutboxPayload.class);
            return new CreditApplyJob(
                    payload.creditApplicationId(),
                    payload.applyId(),
                    payload.partnerUserId(),
                    payload.lat(),
                    payload.lng(),
                    payload.ip(),
                    payload.address(),
                    payload.device(),
                    payload.appList()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize credit apply outbox payload", exception);
        }
    }

    private String serialize(CreditApplyJob job) {
        try {
            return objectMapper.writeValueAsString(new OutboxPayload(
                    job.creditApplicationId(),
                    job.applyId(),
                    job.partnerUserId(),
                    job.lat(),
                    job.lng(),
                    job.ip(),
                    job.address(),
                    job.device(),
                    job.appList()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize credit apply outbox payload", exception);
        }
    }

    private record OutboxPayload(
            long creditApplicationId,
            String applyId,
            String partnerUserId,
            BigDecimal lat,
            BigDecimal lng,
            String ip,
            String address,
            LenderDeviceContext device,
            List<CreditRiskAppInfo> appList
    ) {
    }
}
