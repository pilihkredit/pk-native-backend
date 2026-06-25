package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.ProfileSyncPayload;

public class ProfileSyncHandler {
    private final LenderProfileSyncPort lenderProfileSyncPort;
    private final ProfileSyncPayloadLoader profileSyncPayloadLoader;

    public ProfileSyncHandler(
            LenderProfileSyncPort lenderProfileSyncPort,
            ProfileSyncPayloadLoader profileSyncPayloadLoader
    ) {
        this.lenderProfileSyncPort = lenderProfileSyncPort;
        this.profileSyncPayloadLoader = profileSyncPayloadLoader;
    }

    public void sync(ProfileSyncJob job) {
        ProfileSyncPayloadLoader.validateDevice(job.device());
        ProfileSyncPayload payload = job.payloadSnapshot() == null
                ? profileSyncPayloadLoader.load(job.profileId(), job.module())
                : job.payloadSnapshot();
        lenderProfileSyncPort.syncModule(new LenderProfileSyncPort.LenderProfileSyncCommand(
                job.requestId(),
                job.partnerUserId(),
                job.module(),
                payload,
                job.device()
        ));
    }

    public void syncOrThrow(ProfileSyncJob job) {
        try {
            sync(job);
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
