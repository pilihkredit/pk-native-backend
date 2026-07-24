package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileTongdunRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.auth.MobileNumberValidator;

public class ProfileSyncHandler {
    private final LenderProfileSyncPort lenderProfileSyncPort;
    private final ProfileSyncPayloadLoader profileSyncPayloadLoader;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final ProfileLoginLogRepository profileLoginLogRepository;
    private final ProfileAfRepository profileAfRepository;
    private final ProfileTongdunRepository profileTongdunRepository;

    public ProfileSyncHandler(
            LenderProfileSyncPort lenderProfileSyncPort,
            ProfileSyncPayloadLoader profileSyncPayloadLoader,
            UserProfileBindingRepository userProfileBindingRepository,
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileIdentityRepository profileIdentityRepository,
            ProfileLoginLogRepository profileLoginLogRepository,
            ProfileAfRepository profileAfRepository,
            ProfileTongdunRepository profileTongdunRepository
    ) {
        this.lenderProfileSyncPort = lenderProfileSyncPort;
        this.profileSyncPayloadLoader = profileSyncPayloadLoader;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileIdentityRepository = profileIdentityRepository;
        this.profileLoginLogRepository = profileLoginLogRepository;
        this.profileAfRepository = profileAfRepository;
        this.profileTongdunRepository = profileTongdunRepository;
    }

    public LenderProfileSyncPort.LenderProfileSyncResult sync(ProfileSyncJob job) {
        ProfileSyncPayloadLoader.validateDevice(job.device());
        ProfileSyncPayload payload = job.payloadSnapshot() == null
                ? profileSyncPayloadLoader.load(job.profileId(), job.module())
                : job.payloadSnapshot();
        String mobileNo = requireMobileNo(job.mobileNo());
        LenderProfileSyncPort.LenderProfileSyncResult result = lenderProfileSyncPort.syncModule(
                new LenderProfileSyncPort.LenderProfileSyncCommand(
                job.requestId(),
                job.partnerUserId(),
                mobileNo,
                job.module(),
                payload,
                job.device(),
                job.companions()
        ));
        userProfileBindingRepository.recordLenderProfileSync(job.profileId(), result.externalUserId());
        if (result.externalInteractionId() != null) {
            persistLenderInteraction(
                    job.profileId(),
                    job.requestId(),
                    job.module(),
                    result.externalInteractionId()
            );
            for (LenderProfileSyncPort.SyncCompanion companion : job.companions()) {
                String companionAuditId = companion.auditRequestId() == null || companion.auditRequestId().isBlank()
                        ? job.requestId()
                        : companion.auditRequestId();
                persistLenderInteraction(
                        job.profileId(),
                        companionAuditId,
                        companion.module(),
                        result.externalInteractionId()
                );
                if (companion.module() == ProfileSyncModule.APPSFLYER_INSTALL
                        && companion.auditRequestId() != null
                        && !companion.auditRequestId().isBlank()) {
                    profileAfRepository.findByRequestId(companion.auditRequestId()).ifPresent(af -> {
                        if (af.id() != null && af.profileId() == null) {
                            profileAfRepository.bindProfileIfNull(af.id(), job.profileId(), mobileNo);
                        }
                    });
                }
            }
        }
        return result;
    }

    public LenderProfileSyncPort.LenderProfileSyncResult syncOrThrow(ProfileSyncJob job) {
        try {
            return sync(job);
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception.getMessage());
        }
    }

    private static String requireMobileNo(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "user mobileNo is required for lender profile sync"
            );
        }
        String normalized = mobileNo.trim();
        if (!MobileNumberValidator.isValid(normalized)) {
            throw new ApiException(
                    ApiCode.INVALID_MOBILE_NUMBER,
                    "mobileNo must be Indonesian format (starts with 8, 9-32 digits) for lender sync"
            );
        }
        return normalized;
    }

    private void persistLenderInteraction(
            long profileId,
            String requestId,
            ProfileSyncModule module,
            Long externalInteractionId
    ) {
        switch (module) {
            case PERSONAL -> profilePersonalRepository.updateLastLenderInteraction(
                    profileId,
                    externalInteractionId
            );
            case CONTACT -> profileContactRepository.updateLastLenderInteraction(
                    profileId,
                    externalInteractionId
            );
            case BANK_CARD -> profileBankCardRepository.updateLastLenderInteraction(
                    requestId,
                    externalInteractionId
            );
            case IDENTITY -> profileIdentityRepository.updateLastLenderInteraction(
                    profileId,
                    externalInteractionId
            );
            case LOGIN_LOG -> profileLoginLogRepository.updateLastLenderInteraction(
                    profileId,
                    externalInteractionId
            );
            case APPSFLYER_INSTALL -> profileAfRepository.updateLastLenderInteraction(
                    requestId,
                    externalInteractionId
            );
            case TONGDUN_DEVICE -> profileTongdunRepository.updateLastLenderInteraction(
                    requestId,
                    externalInteractionId
            );
        }
    }
}
