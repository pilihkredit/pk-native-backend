package com.pk.infra.credit;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

public class CreditCallbackIntakeFacade {
    private final CallbackEventRepository callbackEventRepository;
    private final CreditCallbackOutboxPublisher creditCallbackOutboxPublisher;
    private final CreditCallbackParser callbackParser;

    public CreditCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            CreditCallbackOutboxPublisher creditCallbackOutboxPublisher,
            CreditCallbackParser callbackParser
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.creditCallbackOutboxPublisher = creditCallbackOutboxPublisher;
        this.callbackParser = callbackParser;
    }

    public IntakeResult intake(String rawPayloadJson) {
        CreditCallbackParser.ParsedCreditCallback callback = callbackParser.parse(rawPayloadJson);
        String idempotencyKey = buildIdempotencyKey(callback);
        var existing = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new IntakeResult(existing.get().id(), true);
        }
        try {
            long callbackEventId = callbackEventRepository.insert(new CallbackEventRepository.CallbackEventInsert(
                    "CB-" + UUID.randomUUID(),
                    CreditProviderCode.PENDANAAN,
                    CallbackTypes.CREDIT_RESULT,
                    callback.applyId(),
                    idempotencyKey,
                    callback.externalStatus(),
                    rawPayloadJson,
                    CallbackProcessStatus.RECEIVED,
                    Instant.now()
            ));
            creditCallbackOutboxPublisher.publish(callbackEventId, callback.applyId());
            return new IntakeResult(callbackEventId, false);
        } catch (DataIntegrityViolationException exception) {
            var replay = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return new IntakeResult(replay.get().id(), true);
            }
            throw exception;
        }
    }

    private static String buildIdempotencyKey(CreditCallbackParser.ParsedCreditCallback callback) {
        return CreditProviderCode.PENDANAAN
                + ":"
                + CallbackTypes.CREDIT_RESULT
                + ":"
                + callback.applyId()
                + ":"
                + callback.externalStatus()
                + ":"
                + nullToEmpty(callback.creditApplyNo());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record IntakeResult(long callbackEventId, boolean duplicate) {
    }
}
