package com.pk.infra.repay;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepayTrialBackfillService {
    private static final Logger log = LoggerFactory.getLogger(RepayTrialBackfillService.class);
    private static final int FALLBACK_BATCH_SIZE = 50;
    private static final String FALLBACK_ZONE_ID = "Asia/Jakarta";

    private final LoanBillReadRepository loanBillReadRepository;
    private final RepaymentPlanTermRepository repaymentPlanTermRepository;
    private final RepayTrialFacade repayTrialFacade;
    private final UserAuthRepository userAuthRepository;
    private final RepayTrialBackfillProperties properties;

    public RepayTrialBackfillService(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            RepayTrialFacade repayTrialFacade,
            UserAuthRepository userAuthRepository,
            RepayTrialBackfillProperties properties
    ) {
        this.loanBillReadRepository = loanBillReadRepository;
        this.repaymentPlanTermRepository = repaymentPlanTermRepository;
        this.repayTrialFacade = repayTrialFacade;
        this.userAuthRepository = userAuthRepository;
        this.properties = properties;
    }

    public BackfillResult run() {
        return run(configuredBatchSize());
    }

    /**
     * Drains current candidates in one run using loan_application.id cursor paging.
     * Each loan is trialled at most once per run.
     * {@code batchSize} is only the page size, not a total cap.
     */
    public BackfillResult run(int batchSize) {
        int pageSize = batchSize > 0 ? batchSize : configuredBatchSize();
        Instant createdFromInclusive = resolveCreatedFromInclusive();
        long afterId = 0L;
        int candidates = 0;
        int success = 0;
        int failed = 0;
        int skipped = 0;
        int pages = 0;

        log.info(
                "Repay trial backfill start pageSize={} lookbackDays={} createdFromInclusive={}",
                pageSize,
                configuredLookbackDays(),
                createdFromInclusive
        );

        while (true) {
            List<LoanBillReadRepository.TrialBackfillCandidate> due =
                    loanBillReadRepository.findDueForTrialBackfill(afterId, pageSize, createdFromInclusive);
            if (due.isEmpty()) {
                break;
            }
            pages++;
            for (LoanBillReadRepository.TrialBackfillCandidate candidate : due) {
                candidates++;
                try {
                    List<Integer> termNos = pendingTermNos(candidate.loanApplicationId());
                    if (termNos.isEmpty()) {
                        skipped++;
                        afterId = candidate.loanApplicationId();
                        continue;
                    }
                    String mobileNo = userAuthRepository.findByUserId(candidate.userId())
                            .map(profile -> profile.mobileNo())
                            .orElse(null);
                    repayTrialFacade.syncFromLenderForJob(
                            candidate.userId(),
                            mobileNo,
                            new RepayTrialFacade.TrialCommand(
                                    "JOB-" + UUID.randomUUID(),
                                    candidate.loanApplyId(),
                                    termNos,
                                    "NORMAL"
                            )
                    );
                    success++;
                } catch (RuntimeException exception) {
                    failed++;
                    log.warn(
                            "Repay trial backfill failed for loanApplyId={} userId={}",
                            candidate.loanApplyId(),
                            candidate.userId(),
                            exception
                    );
                }
                afterId = candidate.loanApplicationId();
            }
            if (due.size() < pageSize) {
                break;
            }
        }

        return new BackfillResult(candidates, success, failed, skipped, pages, pageSize, createdFromInclusive);
    }

    private List<Integer> pendingTermNos(long loanApplicationId) {
        return repaymentPlanTermRepository.findByLoanApplicationId(loanApplicationId).stream()
                .filter(term -> RepayTermStatus.isPending(term.termStatus()))
                .map(RepaymentPlanTermRepository.TermRecord::termNo)
                .sorted()
                .toList();
    }

    public int configuredBatchSize() {
        int configured = properties == null ? FALLBACK_BATCH_SIZE : properties.batchSize();
        return configured > 0 ? configured : FALLBACK_BATCH_SIZE;
    }

    public int configuredLookbackDays() {
        return properties == null ? 1 : properties.lookbackDays();
    }

    Instant resolveCreatedFromInclusive() {
        int lookbackDays = configuredLookbackDays();
        if (lookbackDays <= 0) {
            return null;
        }
        ZoneId zone = resolveZoneId();
        LocalDate startDay = LocalDate.now(zone).minusDays(lookbackDays - 1L);
        return startDay.atStartOfDay(zone).toInstant();
    }

    private ZoneId resolveZoneId() {
        String zone = properties == null || properties.zoneId() == null || properties.zoneId().isBlank()
                ? FALLBACK_ZONE_ID
                : properties.zoneId().trim();
        try {
            return ZoneId.of(zone);
        } catch (Exception ignored) {
            log.warn("Invalid zoneId={}, fallback={}", zone, FALLBACK_ZONE_ID);
            return ZoneId.of(FALLBACK_ZONE_ID);
        }
    }

    public record BackfillResult(
            int candidates,
            int success,
            int failed,
            int skipped,
            int pages,
            int pageSize,
            Instant createdFromInclusive
    ) {
    }
}
