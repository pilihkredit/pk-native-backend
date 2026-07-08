package com.pk.infra.callback;

import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.CallbackProcessStatus;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public class ServerEventCallbackIntakeFacade {
    private static final Logger log = LoggerFactory.getLogger(ServerEventCallbackIntakeFacade.class);

    private final CallbackEventRepository callbackEventRepository;
    private final ServerEventCallbackParser serverEventCallbackParser;
    private final AppsFlyerS2sReporter appsFlyerS2sReporter;

    public ServerEventCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            ServerEventCallbackParser serverEventCallbackParser,
            AppsFlyerS2sReporter appsFlyerS2sReporter
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.serverEventCallbackParser = serverEventCallbackParser;
        this.appsFlyerS2sReporter = appsFlyerS2sReporter;
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
            reportToAppsFlyer(callbackEventId, callback);
            return new IntakeResult(callbackEventId, false);
        } catch (DataIntegrityViolationException exception) {
            var replay = callbackEventRepository.findByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return new IntakeResult(replay.get().id(), true);
            }
            throw exception;
        }
    }

    private void reportToAppsFlyer(
            long callbackEventId,
            ServerEventCallbackParser.ParsedServerEventCallback callback
    ) {
        try {
            AppsFlyerS2sReporter.ReportResult result = appsFlyerS2sReporter.report(callbackEventId, callback);
            log.info(
                    "AF report for event push: callbackEventId={}, eventType={}, reported={}, message={}",
                    callbackEventId,
                    callback.eventType(),
                    result.reported(),
                    result.message()
            );
        } catch (Exception exception) {
            // Callback already persisted; AF failure must not fail lender callback ACK.
            log.error(
                    "AF report failed after callback persisted: callbackEventId={}, eventType={}, error={}",
                    callbackEventId,
                    callback.eventType(),
                    exception.getMessage(),
                    exception
            );
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
