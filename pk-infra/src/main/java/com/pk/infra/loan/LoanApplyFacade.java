package com.pk.infra.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.LoanAmountValidator;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.LoanQuoteIntegrityValidator;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.ProfileSyncPayloadLoader;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

public class LoanApplyFacade {
    public static final String PUBLIC_PROCESSING = LoanApplicationStatus.PROCESSING;
    private static final String SOURCE = "LOAN_APPLY";

    private final OnboardingProgressFacade onboardingProgressFacade;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    private final LoanQuoteRepository loanQuoteRepository;
    private final LoanQuoteProperties loanQuoteProperties;
    private final LoanProductFacade loanProductFacade;
    private final ProfileVersionRepository profileVersionRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LoanApplyOutboxPublisher loanApplyOutboxPublisher;

    public LoanApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            LoanQuoteRepository loanQuoteRepository,
            LoanQuoteProperties loanQuoteProperties,
            LoanProductFacade loanProductFacade,
            ProfileVersionRepository profileVersionRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanApplyOutboxPublisher loanApplyOutboxPublisher
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditLimitSnapshotRepository = creditLimitSnapshotRepository;
        this.loanQuoteRepository = loanQuoteRepository;
        this.loanQuoteProperties = loanQuoteProperties;
        this.loanProductFacade = loanProductFacade;
        this.profileVersionRepository = profileVersionRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanStatusHistoryRepository = loanStatusHistoryRepository;
        this.loanApplyOutboxPublisher = loanApplyOutboxPublisher;
    }

    public ApplyResult apply(long profileId, String partnerUserId, ApplyCommand command) {
        validate(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = loanApplicationRepository.findByLoanApplyId(command.loanApplyId());
        if (existing.isPresent()) {
            return toApplyResult(existing.get());
        }

        LoanQuoteRepository.LoanQuoteRecord quote = loanQuoteRepository.findByQuoteNo(command.quoteNo())
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        LoanQuoteIntegrityValidator.validateNotExpired(quote.quotedAt(), loanQuoteProperties.ttl());
        int termCount = loanQuoteRepository.countTermsByQuoteId(quote.id());
        LoanQuoteIntegrityValidator.validateIntegrity(quote, termCount);
        LoanQuoteIntegrityValidator.computeHash(quote, termCount);

        CreditApplicationRepository.CreditApplicationRecord creditRecord = creditApplicationRepository
                .findById(quote.creditApplicationId())
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        if (creditRecord.profileId() != profileId) {
            throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
        }
        if (!CreditApplicationStatus.APPROVED.equals(creditRecord.status())) {
            throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
        }

        CreditLimitSnapshotRepository.CreditLimitSnapshotData limits = creditLimitSnapshotRepository
                .findByCreditApplicationId(creditRecord.id())
                .orElseThrow(() -> new ApiException(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE));
        if (limits.contractExpireAt() != null && limits.contractExpireAt().isBefore(Instant.now())) {
            throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
        }

        BigDecimal applyAmt = LoanAmountValidator.normalize(quote.applyAmt());
        LoanAmountValidator.validateAgainstCreditLimits(
                applyAmt,
                limits.riskMinLimit(),
                limits.fakeCreditLimit(),
                limits.borrowAmtStepSize()
        );

        ProductListResolver.ResolvedProductList productSnapshot = loanProductFacade.resolveProductList(
                profileId,
                creditRecord.applyId(),
                true
        );
        loanProductFacade.requireProductRepayMethod(
                productSnapshot,
                quote.productCode(),
                quote.repayMethod()
        );

        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                onboarding.completedModules(),
                SOURCE
        );

        try {
            long loanApplicationId = loanApplicationRepository.insert(new LoanApplicationRepository.LoanApplicationInsert(
                    command.loanApplyId(),
                    creditRecord.id(),
                    quote.id(),
                    profileId,
                    profileVersionId,
                    applyAmt,
                    command.loanPurpose(),
                    LoanApplicationStatus.INIT
            ));
            loanStatusHistoryRepository.insert(
                    loanApplicationId,
                    null,
                    LoanApplicationStatus.INIT,
                    null,
                    SOURCE
            );
            loanApplyOutboxPublisher.publish(new LoanApplyJob(
                    loanApplicationId,
                    command.loanApplyId(),
                    creditRecord.applyId(),
                    applyAmt,
                    quote.productCode(),
                    quote.repayMethod(),
                    command.loanPurpose(),
                    null,
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.adId(),
                    command.device(),
                    command.appList()
            ));
            return new ApplyResult(command.loanApplyId(), PUBLIC_PROCESSING, null);
        } catch (DataIntegrityViolationException exception) {
            var replay = loanApplicationRepository.findByLoanApplyId(command.loanApplyId());
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
        return toStatusResult(record);
    }

    private static void validate(ApplyCommand command) {
        if (command.loanApplyId() == null
                || command.loanApplyId().isBlank()
                || command.loanApplyId().length() > 64
                || command.quoteNo() == null
                || command.quoteNo().isBlank()
                || command.quoteNo().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.appList() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.loanPurpose() != null && command.loanPurpose().length() > 256) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static ApplyResult toApplyResult(LoanApplicationRepository.LoanApplicationRecord record) {
        return new ApplyResult(
                record.loanApplyId(),
                PUBLIC_PROCESSING,
                record.externalLoanApplyNo()
        );
    }

    private static StatusResult toStatusResult(LoanApplicationRepository.LoanApplicationRecord record) {
        Long payTime = record.payTime() == null ? null : record.payTime().toEpochMilli();
        return new StatusResult(
                record.loanApplyId(),
                LoanExternalStatusMapper.publicStatusOf(record),
                record.externalLoanApplyNo(),
                record.billNo(),
                record.applyAmt(),
                record.payAmount(),
                payTime
        );
    }

    public record ApplyCommand(
            String loanApplyId,
            String quoteNo,
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
            String loanApplyNo,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime
    ) {
    }
}
