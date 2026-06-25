package com.pk.infra.credit;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.CreditProviderCode;
import com.pk.core.credit.CreditRiskAppInfo;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
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
    private final CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    private final CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;

    public CreditApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            ProfileVersionRepository profileVersionRepository,
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditStatusHistoryRepository creditStatusHistoryRepository
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.creditApplicationRepository = creditApplicationRepository;
        this.profileVersionRepository = profileVersionRepository;
        this.creditLimitSnapshotRepository = creditLimitSnapshotRepository;
        this.creditApplyOutboxPublisher = creditApplyOutboxPublisher;
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
    }

    public ApplyResult apply(long profileId, String partnerUserId, ApplyCommand command) {
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
                onboarding.completedModules(),
                SOURCE
        );

        try {
            long creditApplicationId = creditApplicationRepository.insert(new CreditApplicationRepository.CreditApplicationInsert(
                    applyId,
                    command.requestId(),
                    CreditProviderCode.PENDANAAN,
                    profileId,
                    profileVersionId,
                    CreditApplicationStatus.INIT
            ));
            creditStatusHistoryRepository.insert(
                    creditApplicationId,
                    null,
                    CreditApplicationStatus.INIT,
                    null,
                    SOURCE
            );
            creditApplyOutboxPublisher.publish(new CreditApplyJob(
                    creditApplicationId,
                    applyId,
                    partnerUserId,
                    command.lat(),
                    command.lng(),
                    command.ip(),
                    command.address(),
                    command.device(),
                    command.appList()
            ));
            return new ApplyResult(applyId, PUBLIC_PROCESSING, null);
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
        var limits = creditLimitSnapshotRepository.findByCreditApplicationId(record.id()).orElse(null);
        return toStatusResult(record, limits);
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
            CreditLimitSnapshotRepository.CreditLimitSnapshotData limits
    ) {
        Long contractExpireTime = limits == null || limits.contractExpireAt() == null
                ? null
                : limits.contractExpireAt().toEpochMilli();
        return new StatusResult(
                record.applyId(),
                CreditExternalStatusMapper.publicStatusOf(record),
                record.externalCreditApplyNo(),
                contractExpireTime,
                limits == null ? null : limits.riskMinLimit(),
                limits == null ? null : limits.riskMaxLimit(),
                limits == null ? null : limits.psychologicalCreditLimit(),
                limits == null ? null : limits.fakeCreditLimit(),
                limits == null ? null : limits.borrowAmtStepSize()
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
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize
    ) {
    }
}
