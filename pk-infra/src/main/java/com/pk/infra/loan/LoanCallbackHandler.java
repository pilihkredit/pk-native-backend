package com.pk.infra.loan;

import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanCallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(LoanCallbackHandler.class);
    private static final String SOURCE = "LOAN_CALLBACK";

    private final CallbackEventRepository callbackEventRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanCallbackParser loanCallbackParser;
    private final LoanLenderStatusApplier loanLenderStatusApplier;

    public LoanCallbackHandler(
            CallbackEventRepository callbackEventRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanCallbackParser loanCallbackParser,
            LoanLenderStatusApplier loanLenderStatusApplier
    ) {
        this.callbackEventRepository = callbackEventRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanCallbackParser = loanCallbackParser;
        this.loanLenderStatusApplier = loanLenderStatusApplier;
    }

    public void handle(LoanCallbackJob job) {
        CallbackEventRepository.CallbackEventRecord callbackEvent = callbackEventRepository
                .findById(job.callbackEventId())
                .orElseThrow(() -> new IllegalStateException("Callback event not found: " + job.callbackEventId()));
        Instant now = Instant.now();
        var application = loanApplicationRepository.findByLoanApplyId(job.loanApplyId());
        if (application.isEmpty()) {
            log.warn("Loan callback ignored because loanApplyId={} was not found", job.loanApplyId());
            callbackEventRepository.markIgnored(callbackEvent.id(), now);
            return;
        }
        LoanCallbackParser.ParsedLoanCallback parsed = loanCallbackParser.parse(callbackEvent.payloadJson());
        LenderLoanStatusPort.LenderLoanStatusResult status = new LenderLoanStatusPort.LenderLoanStatusResult(
                parsed.externalStatus(),
                parsed.loanApplyNo(),
                parsed.billNo(),
                parsed.applyAmt(),
                parsed.payAmount(),
                parsed.payTime(),
                parsed.freezeEndTime(),
                null,
                null
        );
        loanLenderStatusApplier.apply(application.get(), status, SOURCE);
        callbackEventRepository.markProcessed(callbackEvent.id(), now);
    }
}
