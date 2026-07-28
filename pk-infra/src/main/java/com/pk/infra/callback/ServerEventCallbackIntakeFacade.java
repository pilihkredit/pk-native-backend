package com.pk.infra.callback;

import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.port.LenderServerEventCallbackRepository;
import com.pk.core.callback.port.ServerEventCallbackParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public class ServerEventCallbackIntakeFacade {
    private static final Logger log = LoggerFactory.getLogger(ServerEventCallbackIntakeFacade.class);

    private final LenderServerEventCallbackRepository lenderServerEventCallbackRepository;
    private final ServerEventCallbackParser serverEventCallbackParser;
    private final AppsFlyerS2sReporter appsFlyerS2sReporter;

    public ServerEventCallbackIntakeFacade(
            LenderServerEventCallbackRepository lenderServerEventCallbackRepository,
            ServerEventCallbackParser serverEventCallbackParser,
            AppsFlyerS2sReporter appsFlyerS2sReporter
    ) {
        this.lenderServerEventCallbackRepository = lenderServerEventCallbackRepository;
        this.serverEventCallbackParser = serverEventCallbackParser;
        this.appsFlyerS2sReporter = appsFlyerS2sReporter;
    }

    public IntakeResult intake(String rawPayloadJson) {
        ServerEventCallbackParser.ParsedServerEventCallback callback =
                serverEventCallbackParser.parse(rawPayloadJson);
        var existing = lenderServerEventCallbackRepository.findByEventId(callback.eventId());
        if (existing.isPresent()) {
            return new IntakeResult(existing.get().id(), true);
        }
        try {
            long serverEventCallbackId = lenderServerEventCallbackRepository.insert(
                    new LenderServerEventCallbackRepository.LenderServerEventCallbackInsert(
                            callback.eventId(),
                            callback.eventType(),
                            callback.eventTime(),
                            callback.eventValue(),
                            callback.value(),
                            callback.clientId(),
                            callback.userId(),
                            callback.partnerUserId(),
                            callback.appName(),
                            callback.countryCode(),
                            callback.appVersion(),
                            callback.countryName(),
                            callback.deviceNo(),
                            callback.systemPlatform(),
                            callback.adId()
                    )
            );
            reportToAppsFlyer(serverEventCallbackId, callback);
            return new IntakeResult(serverEventCallbackId, false);
        } catch (DataIntegrityViolationException exception) {
            var replay = lenderServerEventCallbackRepository.findByEventId(callback.eventId());
            if (replay.isPresent()) {
                return new IntakeResult(replay.get().id(), true);
            }
            throw exception;
        }
    }

    private void reportToAppsFlyer(
            long serverEventCallbackId,
            ServerEventCallbackParser.ParsedServerEventCallback callback
    ) {
        try {
            AppsFlyerS2sReporter.ReportResult result = appsFlyerS2sReporter.report(serverEventCallbackId, callback);
            log.info(
                    "AF report for event push: serverEventCallbackId={}, eventType={}, reported={}, message={}",
                    serverEventCallbackId,
                    callback.eventType(),
                    result.reported(),
                    result.message()
            );
        } catch (Exception exception) {
            log.error(
                    "AF report failed after callback persisted: serverEventCallbackId={}, eventType={}, error={}",
                    serverEventCallbackId,
                    callback.eventType(),
                    exception.getMessage(),
                    exception
            );
        }
    }

    public record IntakeResult(long serverEventCallbackId, boolean duplicate) {
    }
}
