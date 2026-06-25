package com.pk.infra.loan;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.LoanCallbackParser;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

public class LoanCallbackIntakeFacade {
    private final CallbackEventRepository callbackEventRepository;
    private final LoanCallbackOutboxPublisher loanCallbackOutboxPublisher;
    private final LoanCallbackParser loanCallbackParser;

    public LoanCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            LoanCallbackOutboxPublisher loanCallbackOutboxPublisher,
            LoanCallbackParser loanCallbackParser
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.loanCallbackOutboxPublisher = loanCallbackOutboxPublisher;
        this.loanCallbackParser = loanCallbackParser;
    }

    public IntakeResult intake(String rawPayloadJson) {
        LoanCallbackParser.ParsedLoanCallback callback = loanCallbackParser.parse(rawPayloadJson);
        String idempotencyKey = buildIdempotencyKey(callback);
        var existing = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new IntakeResult(existing.get().id(), true);
        }
        try {
            long callbackEventId = callbackEventRepository.insert(new CallbackEventRepository.CallbackEventInsert(
                    "CB-" + UUID.randomUUID(),
                    CreditProviderCode.PENDANAAN,
                    CallbackTypes.LOAN_RESULT,
                    callback.loanApplyId(),
                    idempotencyKey,
                    callback.externalStatus(),
                    rawPayloadJson,
                    CallbackProcessStatus.RECEIVED,
                    Instant.now()
            ));
            loanCallbackOutboxPublisher.publish(callbackEventId, callback.loanApplyId());
            return new IntakeResult(callbackEventId, false);
        } catch (DataIntegrityViolationException exception) {
            var replay = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return new IntakeResult(replay.get().id(), true);
            }
            throw exception;
        }
    }

    private static String buildIdempotencyKey(LoanCallbackParser.ParsedLoanCallback callback) {
        return CreditProviderCode.PENDANAAN
                + ":"
                + CallbackTypes.LOAN_RESULT
                + ":"
                + callback.loanApplyId()
                + ":"
                + callback.externalStatus()
                + ":"
                + nullToEmpty(callback.loanApplyNo());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record IntakeResult(long callbackEventId, boolean duplicate) {
    }
}
