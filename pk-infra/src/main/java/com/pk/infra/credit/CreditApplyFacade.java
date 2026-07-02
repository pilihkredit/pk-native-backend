package com.pk.infra.credit;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import com.pk.infra.profile.ProfileSyncPayloadLoader;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

public class CreditApplyFacade {
    public static final String PUBLIC_PROCESSING = CreditApplicationStatus.PROCESSING;
    private static final String SOURCE = "CREDIT_APPLY";

    private final OnboardingProgressFacade onboardingProgressFacade;
    private final CreditApplicationRepository creditApplicationRepository;
    private final ProfileVersionRepository profileVersionRepository;
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    private final CreditApplyProperties creditApplyProperties;
    private final CreditApplyHandler creditApplyHandler;
    private final CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;
    private final CreditStatusPollHandler creditStatusPollHandler;

    public CreditApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            ProfileVersionRepository profileVersionRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            CreditApplyProperties creditApplyProperties,
            CreditApplyHandler creditApplyHandler,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditStatusPollHandler creditStatusPollHandler
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.creditApplicationRepository = creditApplicationRepository;
        this.profileVersionRepository = profileVersionRepository;
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
        this.creditApplyProperties = creditApplyProperties;
        this.creditApplyHandler = creditApplyHandler;
        this.creditApplyOutboxPublisher = creditApplyOutboxPublisher;
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
        this.creditStatusPollHandler = creditStatusPollHandler;
    }

    public ApplyResult apply(long profileId, String partnerUserId, String mobileNo, ApplyCommand command) {
        validate(command);
        ProfileSyncPayloadLoader.validateDevice(command.device());

        var existing = creditApplicationRepository.findByRequestId(command.requestId());
        if (existing.isPresent()) {
            return toApplyResult(existing.get());
        }

        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        if (!OnboardingProgressFacade.KYC_SYNCED.equals(onboarding.kycStatus())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        String applyId = CreditApplyIdGenerator.generate();
        long profileVersionId = profileVersionRepository.createSnapshot(
                profileId,
                mobileNo,
                onboarding.completedModules(),
                SOURCE
        );

        try {
            long creditApplicationId = creditApplicationRepository.insert(new CreditApplicationRepository.CreditApplicationInsert(
                    applyId,
                    command.requestId(),
                    CreditProviderCode.PENDANAAN,
                    profileId,
                    mobileNo,
                    profileVersionId,
                    CreditApplicationStatus.INIT
            ));
            creditStatusHistoryRepository.insert(
                    creditApplicationId,
                    mobileNo,
                    null,
                    CreditApplicationStatus.INIT,
                    null,
                    SOURCE
            );
            CreditApplyJob job = new CreditApplyJob(
                    creditApplicationId,
                    applyId,
                    partnerUserId,
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.device(),
                    command.appList()
            );
            String creditApplyNo = creditApplyProperties.inlineEnabled()
                    ? creditApplyHandler.submit(job)
                    : enqueueOutbox(job);
            return new ApplyResult(applyId, PUBLIC_PROCESSING, creditApplyNo);
        } catch (DataIntegrityViolationException exception) {
            var replay = creditApplicationRepository.findByRequestId(command.requestId());
            if (replay.isPresent()) {
                return toApplyResult(replay.get());
            }
            throw new ApiException(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
        }
    }

    public StatusResult getStatus(long profileId, String applyId) {
        CreditApplicationRepository.CreditApplicationRecord record = creditApplicationRepository
                .findByApplyIdAndProfileId(applyId, profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        if (!CreditApplicationStatus.isTerminal(record.status())) {
            creditStatusPollHandler.syncFromLenderForApi(record);
            record = creditApplicationRepository
                    .findByApplyIdAndProfileId(applyId, profileId)
                    .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        }
        var query = creditLenderStatusQueryRepository.findByApplyIdAndProfileId(applyId, profileId).orElse(null);
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
                record.externalCreditApplyNo()
        );
    }

    private static StatusResult toStatusResult(
            CreditApplicationRepository.CreditApplicationRecord record,
            CreditLenderStatusQueryRepository.CreditLenderStatusQueryData query
    ) {
        Long freezeEndTime = record.freezeEndAt() == null ? null : record.freezeEndAt().toEpochMilli();
        return new StatusResult(
                record.applyId(),
                CreditExternalStatusMapper.publicStatusOf(record),
                query == null || query.creditApplyNo() == null ? record.externalCreditApplyNo() : query.creditApplyNo(),
                query == null ? null : query.creditContractExpireTime(),
                query == null || query.freezeEndTime() == null ? freezeEndTime : query.freezeEndTime(),
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
