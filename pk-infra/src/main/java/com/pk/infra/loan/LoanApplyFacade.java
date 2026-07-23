package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.LoanAmountValidator;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.ProfileSyncPayloadLoader;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

public class LoanApplyFacade {
    public static final String PUBLIC_PROCESSING = LoanApplicationStatus.PROCESSING;
    private static final String SOURCE = "LOAN_APPLY";

    private final OnboardingProgressFacade onboardingProgressFacade;
    private final CreditApplicationRepository creditApplicationRepository;
    private final LoanQuoteRepository loanQuoteRepository;
    private final ProfileVersionRepository profileVersionRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanLenderStatusQueryRepository loanLenderStatusQueryRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LoanApplyHandler loanApplyHandler;
    private final LoanApplyProperties loanApplyProperties;
    private final LoanApplyOutboxPublisher loanApplyOutboxPublisher;
    private final LoanStatusPollHandler loanStatusPollHandler;

    public LoanApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            LoanQuoteRepository loanQuoteRepository,
            ProfileVersionRepository profileVersionRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanLenderStatusQueryRepository loanLenderStatusQueryRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanApplyHandler loanApplyHandler,
            LoanApplyProperties loanApplyProperties,
            LoanApplyOutboxPublisher loanApplyOutboxPublisher,
            LoanStatusPollHandler loanStatusPollHandler
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.creditApplicationRepository = creditApplicationRepository;
        this.loanQuoteRepository = loanQuoteRepository;
        this.profileVersionRepository = profileVersionRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanLenderStatusQueryRepository = loanLenderStatusQueryRepository;
        this.loanStatusHistoryRepository = loanStatusHistoryRepository;
        this.loanApplyHandler = loanApplyHandler;
        this.loanApplyProperties = loanApplyProperties;
        this.loanApplyOutboxPublisher = loanApplyOutboxPublisher;
        this.loanStatusPollHandler = loanStatusPollHandler;
    }

    public ApplyResult apply(long profileId, String partnerUserId, String mobileNo, ApplyCommand command) {
        validate(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = loanApplicationRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return toApplyResult(existing.get());
        }

        CreditApplicationRepository.CreditApplicationRecord creditRecord = creditApplicationRepository
                .findByApplyIdAndProfileId(command.applyId(), profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));

        String quoteNo = command.quoteNo().trim();
        LoanQuoteRepository.LoanQuoteRecord quote = loanQuoteRepository.findByQuoteNo(quoteNo).orElse(null);
        ResolvedApplyScalars scalars = resolveScalars(command, quote);
        Long quoteId = quote == null ? null : quote.id();

        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                mobileNo,
                onboarding.completedModules(),
                SOURCE
        );

        String loanApplyId = quoteNo;
        try {
            long loanApplicationId = loanApplicationRepository.insert(new LoanApplicationRepository.LoanApplicationInsert(
                    loanApplyId,
                    command.requestId(),
                    creditRecord.applyId(),
                    mobileNo,
                    creditRecord.id(),
                    quoteId,
                    quoteNo,
                    profileId,
                    profileVersionId,
                    scalars.applyAmt(),
                    scalars.productCode(),
                    scalars.repayMethod(),
                    scalars.couponId(),
                    command.loanPurpose(),
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.adId(),
                    LoanApplicationStatus.INIT
            ));
            loanStatusHistoryRepository.insert(
                    loanApplicationId,
                    null,
                    LoanApplicationStatus.INIT,
                    null,
                    SOURCE
            );
            LoanApplyJob job = new LoanApplyJob(
                    loanApplicationId,
                    loanApplyId,
                    creditRecord.applyId(),
                    scalars.applyAmt(),
                    scalars.productCode(),
                    scalars.repayMethod(),
                    command.loanPurpose(),
                    scalars.couponId(),
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.adId(),
                    command.device(),
                    command.appList()
            );
            String loanApplyNo = loanApplyProperties.inlineEnabled()
                    ? loanApplyHandler.submit(job)
                    : enqueueOutbox(job);
            return new ApplyResult(loanApplyId, PUBLIC_PROCESSING, loanApplyNo);
        } catch (DataIntegrityViolationException exception) {
            var replay = loanApplicationRepository.findByRequestId(command.requestId());
            if (replay.isPresent()) {
                return toApplyResult(replay.get());
            }
            throw new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
        }
    }

    public StatusResult getStatus(long profileId, String loanApplyId) {
        LoanApplicationRepository.LoanApplicationRecord record = loanApplicationRepository
                .findByLoanApplyIdAndProfileId(loanApplyId, profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        if (!LoanApplicationStatus.isTerminal(record.status())) {
            loanStatusPollHandler.syncFromLenderForApi(record);
            record = loanApplicationRepository
                    .findByLoanApplyIdAndProfileId(loanApplyId, profileId)
                    .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        }
        var query = loanLenderStatusQueryRepository
                .findLatestByLoanApplyIdAndProfileId(loanApplyId, profileId)
                .orElse(null);
        return toStatusResult(record, query);
    }

    private String enqueueOutbox(LoanApplyJob job) {
        loanApplyOutboxPublisher.publish(job);
        return null;
    }

    private static void validate(ApplyCommand command) {
        if (command.requestId() == null
                || command.requestId().isBlank()
                || command.requestId().length() > 64
                || command.applyId() == null
                || command.applyId().isBlank()
                || command.applyId().length() > 64
                || command.quoteNo() == null
                || command.quoteNo().isBlank()
                || command.quoteNo().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.productCode() != null && command.productCode().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.repayMethod() != null && command.repayMethod().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.appList() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.loanPurpose() != null && command.loanPurpose().length() > 256) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.address() != null && command.address().length() > 512) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    /**
     * Prefer request scalars when present; otherwise fill from trial quote snapshot.
     * Published BFF contract: frontend may omit these and only send {@code quoteNo}.
     */
    private static ResolvedApplyScalars resolveScalars(
            ApplyCommand command,
            LoanQuoteRepository.LoanQuoteRecord quote
    ) {
        BigDecimal applyAmt = command.applyAmt();
        String productCode = blankToNull(command.productCode());
        String repayMethod = blankToNull(command.repayMethod());
        Long couponId = command.couponId();
        if (quote != null) {
            if (applyAmt == null) {
                applyAmt = quote.applyAmt();
            }
            if (productCode == null) {
                productCode = blankToNull(quote.productCode());
            }
            if (repayMethod == null) {
                repayMethod = blankToNull(quote.repayMethod());
            }
            if (couponId == null) {
                couponId = quote.couponId();
            }
        }
        if (applyAmt == null || productCode == null || repayMethod == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return new ResolvedApplyScalars(
                LoanAmountValidator.normalize(applyAmt),
                productCode,
                repayMethod,
                couponId
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record ResolvedApplyScalars(
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId
    ) {
    }

    private static ApplyResult toApplyResult(LoanApplicationRepository.LoanApplicationRecord record) {
        return new ApplyResult(
                record.loanApplyId(),
                PUBLIC_PROCESSING,
                record.externalLoanApplyNo()
        );
    }

    private static StatusResult toStatusResult(
            LoanApplicationRepository.LoanApplicationRecord record,
            LoanLenderStatusQueryRepository.LoanLenderStatusQueryData query
    ) {
        Long payTime = record.payTime() == null ? null : record.payTime().toEpochMilli();
        if (query != null && query.payTime() != null) {
            payTime = query.payTime().toEpochMilli();
        }
        return new StatusResult(
                record.loanApplyId(),
                LoanExternalStatusMapper.publicStatusOf(record),
                firstNonBlank(query == null ? null : query.externalStatus(), record.externalStatus()),
                firstNonBlank(query == null ? null : query.externalLoanApplyNo(), record.externalLoanApplyNo()),
                firstNonBlank(query == null ? null : query.billNo(), record.billNo()),
                query == null || query.applyAmt() == null ? record.applyAmt() : query.applyAmt(),
                query == null || query.payAmount() == null ? record.payAmount() : query.payAmount(),
                payTime,
                query == null ? null : query.freezeEndTime()
        );
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }

    public record ApplyCommand(
            String requestId,
            String applyId,
            String quoteNo,
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId,
            String loanPurpose,
            BigDecimal lat,
            BigDecimal lng,
            String ip,
            String address,
            String adId,
            LenderDeviceContext device,
            List<CreditRiskAppInfo> appList
    ) {
    }

    public record ApplyResult(String loanApplyId, String status, String loanApplyNo) {
    }

    public record StatusResult(
            String loanApplyId,
            String status,
            String applyStatus,
            String loanApplyNo,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime,
            Long freezeEndTime
    ) {
    }
}
