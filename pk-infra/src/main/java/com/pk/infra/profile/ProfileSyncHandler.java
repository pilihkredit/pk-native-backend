package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.auth.MobileNumberValidator;

public class ProfileSyncHandler {
    private final LenderProfileSyncPort lenderProfileSyncPort;
    private final ProfileSyncPayloadLoader profileSyncPayloadLoader;
    private final UserAuthRepository userAuthRepository;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileIdentityRepository profileIdentityRepository;
    private final LenderSyncAuditRequestBuilder lenderSyncAuditRequestBuilder;

    public ProfileSyncHandler(
            LenderProfileSyncPort lenderProfileSyncPort,
            ProfileSyncPayloadLoader profileSyncPayloadLoader,
            UserAuthRepository userAuthRepository,
            UserProfileBindingRepository userProfileBindingRepository,
            ProfilePersonalRepository profilePersonalRepository,
            ProfileContactRepository profileContactRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileIdentityRepository profileIdentityRepository,
            LenderSyncAuditRequestBuilder lenderSyncAuditRequestBuilder
    ) {
        this.lenderProfileSyncPort = lenderProfileSyncPort;
        this.profileSyncPayloadLoader = profileSyncPayloadLoader;
        this.userAuthRepository = userAuthRepository;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileIdentityRepository = profileIdentityRepository;
        this.lenderSyncAuditRequestBuilder = lenderSyncAuditRequestBuilder;
    }

    public LenderProfileSyncPort.LenderProfileSyncResult sync(ProfileSyncJob job) {
        ProfileSyncPayloadLoader.validateDevice(job.device());
        ProfileSyncPayload payload = job.payloadSnapshot() == null
                ? profileSyncPayloadLoader.load(job.profileId(), job.module())
                : job.payloadSnapshot();
        String mobileNo = userAuthRepository.findByProfileId(job.profileId())
                .map(profile -> profile.mobileNo())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "user mobileNo is required for lender profile sync"
                ));
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(
                    ApiCode.INVALID_MOBILE_NUMBER,
                    "mobileNo must be Indonesian format (starts with 8, 9-32 digits) for lender sync"
            );
        }
        LenderProfileSyncPort.LenderProfileSyncResult result = lenderProfileSyncPort.syncModule(
                new LenderProfileSyncPort.LenderProfileSyncCommand(
                job.requestId(),
                job.partnerUserId(),
                mobileNo,
                job.module(),
                payload,
                job.device()
        ));
        userProfileBindingRepository.recordLenderProfileSync(job.profileId(), result.externalUserId());
        if (result.responseDataJson() != null) {
            String auditRequestJson = lenderSyncAuditRequestBuilder.buildProfileUpsertAudit(
                    job.requestId(),
                    job.partnerUserId(),
                    mobileNo,
                    job.module(),
                    payload,
                    job.device(),
                    job.profileId()
            );
            persistLenderAudit(job.profileId(), job.module(), auditRequestJson, result.responseDataJson());
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

    private void persistLenderAudit(
            long profileId,
            ProfileSyncModule module,
            String requestDataJson,
            String responseDataJson
    ) {
        switch (module) {
            case PERSONAL -> profilePersonalRepository.updateLastLenderAudit(
                    profileId,
                    requestDataJson,
                    responseDataJson
            );
            case CONTACT -> profileContactRepository.updateLastLenderAudit(
                    profileId,
                    requestDataJson,
                    responseDataJson
            );
            case BANK_CARD -> profileBankCardRepository.updateLastLenderAudit(
                    profileId,
                    requestDataJson,
                    responseDataJson
            );
            case IDENTITY -> profileIdentityRepository.updateLastLenderAudit(
                    profileId,
                    requestDataJson,
                    responseDataJson
            );
        }
    }
}
