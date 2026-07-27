package com.pk.infra.credit;

import com.pk.core.api.ApiCode;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.ExternalInteractionCallbackLog;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.infra.external.ExternalInteractionCallbackSupport;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public class CreditCallbackIntakeFacade {
    private static final Logger log = LoggerFactory.getLogger(CreditCallbackIntakeFacade.class);
    private static final String SOURCE = "CREDIT_CALLBACK";
    private static final String LIMIT_SOURCE = "LENDER_CALLBACK";
    private static final String HTTP_METHOD = "POST";
    private static final String ENDPOINT = "/callback/credit/result";
    private static final String SUCCESS_RESPONSE = "{\"code\":\"000000\",\"msg\":\"success\"}";

    private final ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository;
    private final CreditCallbackParser callbackParser;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditLenderStatusApplier creditLenderStatusApplier;
    private final UserAuthRepository userAuthRepository;

    public CreditCallbackIntakeFacade(
            ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository,
            CreditCallbackParser callbackParser,
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusApplier creditLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        this.externalInteractionCallbackLogRepository = externalInteractionCallbackLogRepository;
        this.callbackParser = callbackParser;
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditLenderStatusApplier = creditLenderStatusApplier;
        this.userAuthRepository = userAuthRepository;
    }

    public IntakeResult intake(String rawPayloadJson) {
        long startedAt = System.currentTimeMillis();
        CreditCallbackParser.ParsedCreditCallback callback = callbackParser.parse(rawPayloadJson);
        String idempotencyKey = buildIdempotencyKey(callback);
        Optional<Long> existingId = externalInteractionCallbackLogRepository.findIdByIdempotencyKey(idempotencyKey);
        if (existingId.isPresent()) {
            return new IntakeResult(existingId.get(), true, false);
        }

        String interactionNo = UUID.randomUUID().toString();
        String requestBody = rawPayloadJson == null ? "" : rawPayloadJson;
        long interactionCallbackId;
        try {
            interactionCallbackId = externalInteractionCallbackLogRepository.insert(new ExternalInteractionCallbackLog(
                    CreditProviderCode.PENDANAAN,
                    interactionNo,
                    idempotencyKey,
                    CallbackTypes.CREDIT_RESULT,
                    callback.applyId(),
                    null,
                    null,
                    HTTP_METHOD,
                    ENDPOINT,
                    interactionNo,
                    ExternalInteractionCallbackSupport.sha256Hex(requestBody),
                    ExternalInteractionCallbackSupport.truncate(requestBody),
                    null,
                    null,
                    null,
                    false,
                    0
            ));
        } catch (DataIntegrityViolationException exception) {
            Optional<Long> replay = externalInteractionCallbackLogRepository.findIdByIdempotencyKey(idempotencyKey);
            if (replay.isPresent()) {
                return new IntakeResult(replay.get(), true, false);
            }
            throw exception;
        }

        Optional<CreditApplicationRepository.CreditApplicationRecord> application =
                creditApplicationRepository.findByApplyId(callback.applyId());
        if (application.isEmpty()) {
            log.warn("Credit callback ignored because applyId={} was not found", callback.applyId());
            finalizeCallbackLog(interactionCallbackId, null, null, startedAt, true);
            return new IntakeResult(interactionCallbackId, false, true);
        }

        CreditApplicationRepository.CreditApplicationRecord record = application.get();
        LenderCreditPort.LenderCreditStatusResult status = new LenderCreditPort.LenderCreditStatusResult(
                callback.externalStatus(),
                null,
                callback.creditApplyNo(),
                callback.creditContractExpireTime(),
                callback.freezeEndTime(),
                callback.riskMinLimit(),
                callback.riskMaxLimit(),
                callback.psychologicalCreditLimit(),
                callback.fakeCreditLimit(),
                callback.borrowAmtStepSize(),
                null
        );
        creditLenderStatusApplier.apply(record, status, SOURCE, LIMIT_SOURCE, interactionCallbackId);
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        finalizeCallbackLog(interactionCallbackId, mobileNo, record.userId(), startedAt, true);
        return new IntakeResult(interactionCallbackId, false, false);
    }

    private void finalizeCallbackLog(
            long interactionCallbackId,
            String mobileNo,
            Long userId,
            long startedAt,
            boolean success
    ) {
        externalInteractionCallbackLogRepository.updateResponse(
                interactionCallbackId,
                mobileNo,
                userId,
                ApiCode.SUCCESS.code(),
                ApiCode.SUCCESS.message(),
                SUCCESS_RESPONSE,
                success,
                (int) Math.max(0, System.currentTimeMillis() - startedAt)
        );
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

    public record IntakeResult(long externalInteractionCallbackId, boolean duplicate, boolean ignored) {
    }
}
