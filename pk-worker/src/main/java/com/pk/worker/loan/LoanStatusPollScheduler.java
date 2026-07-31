package com.pk.worker.loan;

import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.infra.loan.LoanStatusPollHandler;
import com.pk.worker.config.WorkerOutboxProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(LoanStatusPollHandler.class)
public class LoanStatusPollScheduler {
    private static final Logger log = LoggerFactory.getLogger(LoanStatusPollScheduler.class);

    private final WorkerOutboxProperties workerOutboxProperties;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanStatusPollHandler loanStatusPollHandler;

    public LoanStatusPollScheduler(
            WorkerOutboxProperties workerOutboxProperties,
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusPollHandler loanStatusPollHandler
    ) {
        this.workerOutboxProperties = workerOutboxProperties;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanStatusPollHandler = loanStatusPollHandler;
    }

    @Scheduled(fixedDelayString = "${pk.worker.loan.poll-interval-ms:30000}")
    public void poll() {
        if (!workerOutboxProperties.enabled()) {
            return;
        }
        List<LoanApplicationRepository.LoanApplicationRecord> due =
                loanApplicationRepository.findPendingPoll(workerOutboxProperties.batchSize());
        for (LoanApplicationRepository.LoanApplicationRecord record : due) {
            try {
                loanStatusPollHandler.poll(record);
            } catch (RuntimeException exception) {
                log.warn("Loan status poll failed for loanApplyId={}", record.loanApplyId(), exception);
            }
        }
    }
}
