package com.pk.infra.credit;

import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(CreditCallbackHandler.class);
    private static final String SOURCE = "CREDIT_CALLBACK";
    private static final String LIMIT_SOURCE = "LENDER_CALLBACK";

    private final CallbackEventRepository callbackEventRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditCallbackParser callbackParser;
    private final CreditLenderStatusApplier creditLenderStatusApplier;

    public CreditCallbackHandler(
            CallbackEventRepository callbackEventRepository,
            CreditApplicationRepository creditApplicationRepository,
            CreditCallbackParser callbackParser,
            CreditLenderStatusApplier creditLenderStatusApplier
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.creditApplicationRepository = creditApplicationRepository;
        this.callbackParser = callbackParser;
        this.creditLenderStatusApplier = creditLenderStatusApplier;
    }

    public void handle(CreditCallbackJob job) {
        CallbackEventRepository.CallbackEventRecord callbackEvent = callbackEventRepository
                .findById(job.callbackEventId())
                .orElseThrow(() -> new IllegalStateException("Callback event not found: " + job.callbackEventId()));
        Instant now = Instant.now();
        var application = creditApplicationRepository.findByApplyId(job.applyId());
        if (application.isEmpty()) {
            log.warn("Credit callback ignored because applyId={} was not found", job.applyId());
            callbackEventRepository.markIgnored(callbackEvent.id(), now);
            return;
        }
        CreditCallbackParser.ParsedCreditCallback parsed = callbackParser.parse(callbackEvent.payloadJson());
        LenderCreditPort.LenderCreditStatusResult status = new LenderCreditPort.LenderCreditStatusResult(
                parsed.externalStatus(),
                null,
                parsed.creditApplyNo(),
                parsed.creditContractExpireTime(),
                parsed.freezeEndTime(),
                parsed.riskMinLimit(),
                parsed.riskMaxLimit(),
                parsed.psychologicalCreditLimit(),
                parsed.fakeCreditLimit(),
                parsed.borrowAmtStepSize(),
                null,
                callbackEvent.payloadJson()
        );
        creditLenderStatusApplier.apply(application.get(), status, SOURCE, LIMIT_SOURCE);
        callbackEventRepository.markProcessed(callbackEvent.id(), now);
    }
}
