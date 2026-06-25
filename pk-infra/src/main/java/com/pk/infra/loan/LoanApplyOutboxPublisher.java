package com.pk.infra.loan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.outbox.OutboxEventTypes;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class LoanApplyOutboxPublisher {
    private static final String AGGREGATE_TYPE = "loan_application";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public LoanApplyOutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(LoanApplyJob job) {
        outboxEventRepository.insertPending(new OutboxEventRepository.OutboxEventDraft(
                UUID.randomUUID().toString(),
                OutboxEventTypes.LOAN_APPLY,
                AGGREGATE_TYPE,
                job.loanApplyId(),
                serialize(job)
        ));
    }

    public LoanApplyJob deserialize(String payloadJson) {
        try {
            OutboxPayload payload = objectMapper.readValue(payloadJson, OutboxPayload.class);
            return new LoanApplyJob(
                    payload.loanApplicationId(),
                    payload.loanApplyId(),
                    payload.creditApplyId(),
                    payload.applyAmt(),
                    payload.productCode(),
                    payload.repayMethod(),
                    payload.loanPurpose(),
                    payload.couponId(),
                    payload.lat(),
                    payload.lng(),
                    payload.ip(),
                    payload.address(),
                    payload.adId(),
                    payload.device(),
                    payload.appList()
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize loan apply outbox payload", exception);
        }
    }

    private String serialize(LoanApplyJob job) {
        try {
            return objectMapper.writeValueAsString(new OutboxPayload(
                    job.loanApplicationId(),
                    job.loanApplyId(),
                    job.creditApplyId(),
                    job.applyAmt(),
                    job.productCode(),
                    job.repayMethod(),
                    job.loanPurpose(),
                    job.couponId(),
                    job.lat(),
                    job.lng(),
                    job.ip(),
                    job.address(),
                    job.adId(),
                    job.device(),
                    job.appList()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize loan apply outbox payload", exception);
        }
    }

    private record OutboxPayload(
            long loanApplicationId,
            String loanApplyId,
            String creditApplyId,
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            String loanPurpose,
            Long couponId,
            BigDecimal lat,
            BigDecimal lng,
            String ip,
            String address,
            String adId,
            LenderDeviceContext device,
            List<CreditRiskAppInfo> appList
    ) {
    }
}
