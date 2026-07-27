package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.callback.CallbackTypes;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.external.ExternalInteractionCallbackLog;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.infra.external.ExternalInteractionCallbackSupport;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public class LoanCallbackIntakeFacade {
    private static final Logger log = LoggerFactory.getLogger(LoanCallbackIntakeFacade.class);
    private static final String SOURCE = "LOAN_CALLBACK";
    private static final String HTTP_METHOD = "POST";
    private static final String ENDPOINT = "/callback/loan/result";
    private static final String SUCCESS_RESPONSE = "{\"code\":\"000000\",\"msg\":\"success\"}";

    private final ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository;
    private final LoanCallbackParser loanCallbackParser;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanLenderStatusApplier loanLenderStatusApplier;
    private final UserAuthRepository userAuthRepository;

    public LoanCallbackIntakeFacade(
            ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository,
            LoanCallbackParser loanCallbackParser,
            LoanApplicationRepository loanApplicationRepository,
            LoanLenderStatusApplier loanLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        this.externalInteractionCallbackLogRepository = externalInteractionCallbackLogRepository;
        this.loanCallbackParser = loanCallbackParser;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanLenderStatusApplier = loanLenderStatusApplier;
        this.userAuthRepository = userAuthRepository;
    }

    public IntakeResult intake(String rawPayloadJson) {
        long startedAt = System.currentTimeMillis();
        LoanCallbackParser.ParsedLoanCallback callback = loanCallbackParser.parse(rawPayloadJson);
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
                    CallbackTypes.LOAN_RESULT,
                    callback.loanApplyId(),
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

        Optional<LoanApplicationRepository.LoanApplicationRecord> application =
                loanApplicationRepository.findByLoanApplyId(callback.loanApplyId());
        if (application.isEmpty()) {
            log.warn("Loan callback ignored because loanApplyId={} was not found", callback.loanApplyId());
            finalizeCallbackLog(interactionCallbackId, null, startedAt, true);
            return new IntakeResult(interactionCallbackId, false, true);
        }

        LoanApplicationRepository.LoanApplicationRecord record = application.get();
        LenderLoanStatusPort.LenderLoanStatusResult status = new LenderLoanStatusPort.LenderLoanStatusResult(
                callback.externalStatus(),
                callback.loanApplyNo(),
                callback.billNo(),
                callback.applyAmt(),
                callback.payAmount(),
                callback.payTime(),
                callback.freezeEndTime(),
                null
        );
        loanLenderStatusApplier.apply(record, status, SOURCE, interactionCallbackId);
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        finalizeCallbackLog(interactionCallbackId, mobileNo, startedAt, true);
        return new IntakeResult(interactionCallbackId, false, false);
    }

    private void finalizeCallbackLog(long interactionCallbackId, String mobileNo, long startedAt, boolean success) {
        externalInteractionCallbackLogRepository.updateResponse(
                interactionCallbackId,
                mobileNo,
                ApiCode.SUCCESS.code(),
                ApiCode.SUCCESS.message(),
                SUCCESS_RESPONSE,
                success,
                (int) Math.max(0, System.currentTimeMillis() - startedAt)
        );
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

    public record IntakeResult(long externalInteractionCallbackId, boolean duplicate, boolean ignored) {
    }
}
