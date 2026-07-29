package com.pk.infra.credit;

import com.pk.core.api.ApiException;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.profile.AppsFlyerLenderPayloadResolver;
import com.pk.infra.profile.ProfileSyncJob;
import com.pk.infra.profile.ProfileSyncOrchestrator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Before credit apply: upsert appsFlyerInstall from appsflyer_callback (device_no + conversion_type=install).
 * Failures are logged and never block credit apply.
 */
public class CreditAppsFlyerPreSync {
    private static final Logger log = LoggerFactory.getLogger(CreditAppsFlyerPreSync.class);

    private final AppsFlyerLenderPayloadResolver appsFlyerLenderPayloadResolver;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final CreditApplicationRepository creditApplicationRepository;

    public CreditAppsFlyerPreSync(
            AppsFlyerLenderPayloadResolver appsFlyerLenderPayloadResolver,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            CreditApplicationRepository creditApplicationRepository
    ) {
        this.appsFlyerLenderPayloadResolver = appsFlyerLenderPayloadResolver;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.creditApplicationRepository = creditApplicationRepository;
    }

    public void syncBeforeCreditApply(CreditApplyJob job) {
        LenderDeviceContext device = job.device();
        String deviceNo = device == null ? null : device.deviceNo();
        Optional<ProfileSyncPayload.AppsFlyerInstallPayload> payload =
                appsFlyerLenderPayloadResolver.resolveInstallByDeviceNo(deviceNo);
        if (payload.isEmpty()) {
            log.warn(
                    "AppsFlyer data not found applyId={} deviceNo={}",
                    job.applyId(),
                    deviceNo == null ? "" : deviceNo
            );
            return;
        }
        Optional<CreditApplicationRepository.CreditApplicationRecord> application =
                creditApplicationRepository.findById(job.creditApplicationId());
        if (application.isEmpty()) {
            log.warn(
                    "AppsFlyer pre-sync skipped: credit application missing applyId={} creditApplicationId={}",
                    job.applyId(),
                    job.creditApplicationId()
            );
            return;
        }
        try {
            profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                    application.get().userId(),
                    job.partnerUserId(),
                    job.mobileNo(),
                    job.applyId(),
                    ProfileSyncModule.APPSFLYER_INSTALL,
                    device,
                    payload.get()
            ));
        } catch (ApiException exception) {
            log.warn(
                    "AppsFlyer upsert failed before credit apply applyId={} deviceNo={} code={} detail={}",
                    job.applyId(),
                    deviceNo == null ? "" : deviceNo,
                    exception.apiCode() == null ? "" : exception.apiCode().code(),
                    exception.detail() == null ? exception.getMessage() : exception.detail()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "AppsFlyer upsert failed before credit apply applyId={} deviceNo={} error={}",
                    job.applyId(),
                    deviceNo == null ? "" : deviceNo,
                    exception.getMessage()
            );
        }
    }
}
