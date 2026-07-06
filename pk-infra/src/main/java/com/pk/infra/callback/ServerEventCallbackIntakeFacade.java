package com.pk.infra.callback;

import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

public class ServerEventCallbackIntakeFacade {
    private final CallbackEventRepository callbackEventRepository;
    private final ServerEventCallbackParser serverEventCallbackParser;

    public ServerEventCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            ServerEventCallbackParser serverEventCallbackParser
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.serverEventCallbackParser = serverEventCallbackParser;
    }

    public IntakeResult intake(String rawPayloadJson) {
        ServerEventCallbackParser.ParsedServerEventCallback callback =
                serverEventCallbackParser.parse(rawPayloadJson);
        String idempotencyKey = buildIdempotencyKey(callback);
        var existing = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new IntakeResult(existing.get().id(), true);
        }
        try {
            long callbackEventId = callbackEventRepository.insert(new CallbackEventRepository.CallbackEventInsert(
                    "CB-" + UUID.randomUUID(),
                    CreditProviderCode.PENDANAAN,
                    CallbackTypes.EVENT_PUSH,
                    callback.eventId(),
                    idempotencyKey,
                    callback.eventType(),
                    rawPayloadJson,
                    CallbackProcessStatus.PROCESSED,
                    Instant.now()
            ));
            return new IntakeResult(callbackEventId, false);
        } catch (DataIntegrityViolationException exception) {
            var replay = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return new IntakeResult(replay.get().id(), true);
            }
            throw exception;
        }
    }

    private static String buildIdempotencyKey(ServerEventCallbackParser.ParsedServerEventCallback callback) {
        return CreditProviderCode.PENDANAAN
                + ":"
                + CallbackTypes.EVENT_PUSH
                + ":"
                + callback.eventId();
    }

    public record IntakeResult(long callbackEventId, boolean duplicate) {
    }
}
