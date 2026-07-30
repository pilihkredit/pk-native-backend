package com.pk.infra.credit;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.provider.port.PkProviderRepository;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.infra.profile.OnboardingProgressFacade;
import com.pk.infra.profile.ProfileSyncPayloadLoader;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

public class CreditApplyFacade {
    public static final String PUBLIC_PROCESSING = CreditApplicationStatus.PROCESSING;

    private final OnboardingProgressFacade onboardingProgressFacade;
    private final CreditApplicationRepository creditApplicationRepository;
    private final PkProviderRepository pkProviderRepository;
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    private final CreditApplyProperties creditApplyProperties;
    private final CreditApplyHandler creditApplyHandler;
    private final CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    private final CreditStatusPollHandler creditStatusPollHandler;
    private final ReviewSandboxConfigPort reviewSandboxConfigPort;
    private final String configuredProviderCode;

    public CreditApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            PkProviderRepository pkProviderRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            CreditApplyProperties creditApplyProperties,
            CreditApplyHandler creditApplyHandler,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditStatusPollHandler creditStatusPollHandler,
            ReviewSandboxConfigPort reviewSandboxConfigPort,
            String configuredProviderCode
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.creditApplicationRepository = creditApplicationRepository;
        this.pkProviderRepository = pkProviderRepository;
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
        this.creditApplyProperties = creditApplyProperties;
        this.creditApplyHandler = creditApplyHandler;
        this.creditApplyOutboxPublisher = creditApplyOutboxPublisher;
        this.creditStatusPollHandler = creditStatusPollHandler;
        this.reviewSandboxConfigPort = reviewSandboxConfigPort;
        this.configuredProviderCode = configuredProviderCode;
    }

    public ApplyResult apply(long userId, String partnerUserId, String mobileNo, ApplyCommand command) {
        validate(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = creditApplicationRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return toApplyResult(existing.get());
        }

        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                userId,
                partnerUserId
        );
        if (!OnboardingProgressFacade.KYC_SYNCED.equals(onboarding.kycStatus())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        String providerCode = pkProviderRepository.findActiveProviderCode(configuredProviderCode)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));

        String applyId = CreditApplyIdGenerator.generate();

        try {
            long creditApplicationId = creditApplicationRepository.insert(new CreditApplicationRepository.CreditApplicationInsert(
                    applyId,
                    command.requestId(),
                    providerCode,
                    userId
            ));
            CreditApplyJob job = new CreditApplyJob(
                    creditApplicationId,
                    applyId,
                    partnerUserId,
                    mobileNo,
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.device(),
                    command.appList()
            );
            boolean reviewUser = reviewSandboxConfigPort.findEnabledScenario(mobileNo).isPresent();
            String creditApplyNo = reviewUser || creditApplyProperties.inlineEnabled()
                    ? creditApplyHandler.submit(job)
                    : enqueueOutbox(job);
            if (reviewUser) {
                CreditApplicationRepository.CreditApplicationRecord reviewRecord = creditApplicationRepository
                        .findByApplyIdAndUserId(applyId, userId)
                        .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
                creditStatusPollHandler.syncFromLenderForApi(reviewRecord);
                return new ApplyResult(applyId, CreditApplicationStatus.APPROVED, creditApplyNo);
            }
            return new ApplyResult(applyId, PUBLIC_PROCESSING, creditApplyNo);
        } catch (DataIntegrityViolationException exception) {
            var replay = creditApplicationRepository.findByRequestId(command.requestId());
            if (replay.isPresent()) {
                return toApplyResult(replay.get());
            }
            throw new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
        }
    }

    public StatusResult getStatus(long userId) {
        CreditApplicationRepository.CreditApplicationRecord record = creditApplicationRepository
                .findLatestByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        creditStatusPollHandler.syncFromLenderForApi(record);
        record = creditApplicationRepository
                .findByApplyIdAndUserId(record.applyId(), userId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        var query = creditLenderStatusQueryRepository
                .findLatestByApplyIdAndUserId(record.applyId(), userId)
                .orElse(null);
        return toStatusResult(record, query);
    }

    private String enqueueOutbox(CreditApplyJob job) {
        creditApplyOutboxPublisher.publish(job);
        return null;
    }

    private static void validate(ApplyCommand command) {
        if (command.requestId() == null || command.requestId().isBlank() || command.requestId().length() > 64) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (command.appList() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static ApplyResult toApplyResult(CreditApplicationRepository.CreditApplicationRecord record) {
        return new ApplyResult(
                record.applyId(),
                PUBLIC_PROCESSING,
                record.applyNo()
        );
    }

    private static StatusResult toStatusResult(
            CreditApplicationRepository.CreditApplicationRecord record,
            CreditLenderStatusQueryRepository.CreditLenderStatusQueryData query
    ) {
        String status = PUBLIC_PROCESSING;
        if (query != null && query.externalStatus() != null && !query.externalStatus().isBlank()) {
            status = CreditExternalStatusMapper.toPublicStatus(
                    CreditExternalStatusMapper.mapLenderStatus(query.externalStatus())
            );
        }
        return new StatusResult(
                record.applyId(),
                status,
                query == null || query.creditApplyNo() == null ? record.applyNo() : query.creditApplyNo(),
                query == null ? null : query.creditContractExpireTime(),
                query == null ? null : query.freezeEndTime(),
                query == null ? null : query.riskMinLimit(),
                query == null ? null : query.riskMaxLimit(),
                query == null ? null : query.psychologicalCreditLimit(),
                query == null ? null : query.fakeCreditLimit(),
                query == null ? null : query.borrowAmtStepSize()
        );
    }

    public record ApplyCommand(
            String requestId,
            BigDecimal lat,
            BigDecimal lng,
            String ip,
            String address,
            LenderDeviceContext device,
            List<CreditRiskAppInfo> appList
    ) {
    }

    public record ApplyResult(String applyId, String status, String creditApplyNo) {
    }

    public record StatusResult(
            String applyId,
            String status,
            String creditApplyNo,
            Long creditContractExpireTime,
            Long freezeEndTime,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize
    ) {
    }
}
