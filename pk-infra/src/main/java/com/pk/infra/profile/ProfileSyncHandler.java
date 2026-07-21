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
import com.pk.infra.ocr.OcrSensitiveJsonSupport;

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
    private final LenderSyncAuditRequestBuilder lenderSyncAuditRequestBuilder;
    private final OcrSensitiveJsonSupport ocrSensitiveJsonSupport;

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
            ProfileTongdunRepository profileTongdunRepository,
            LenderSyncAuditRequestBuilder lenderSyncAuditRequestBuilder,
            OcrSensitiveJsonSupport ocrSensitiveJsonSupport
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
        this.lenderSyncAuditRequestBuilder = lenderSyncAuditRequestBuilder;
        this.ocrSensitiveJsonSupport = ocrSensitiveJsonSupport;
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
            persistLenderAudit(
                    job.profileId(),
                    job.requestId(),
                    mobileNo,
                    job.module(),
                    auditRequestJson,
                    result.responseDataJson()
            );
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

    private void persistLenderAudit(
            long profileId,
            String requestId,
            String mobileNo,
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
                    ocrSensitiveJsonSupport.sanitizeForStorage(requestDataJson, mobileNo),
                    ocrSensitiveJsonSupport.sanitizeForStorage(responseDataJson, mobileNo)
            );
            case LOGIN_LOG -> profileLoginLogRepository.updateLastLenderAudit(
                    profileId,
                    requestDataJson,
                    responseDataJson
            );
            case APPSFLYER_INSTALL -> profileAfRepository.updateLastLenderAudit(
                    requestId,
                    requestDataJson,
                    responseDataJson
            );
            case TONGDUN_DEVICE -> profileTongdunRepository.updateLastLenderAudit(
                    requestId,
                    requestDataJson,
                    responseDataJson
            );
        }
    }
}
