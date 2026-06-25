package com.pk.infra.profile;

public class ProfileSyncOrchestrator {
    private final ProfileSyncHandler profileSyncHandler;

    public ProfileSyncOrchestrator(ProfileSyncHandler profileSyncHandler) {
        this.profileSyncHandler = profileSyncHandler;
    }

    /** Synchronously upserts the saved module to the lender before returning to the client. */
    public void scheduleAfterSave(ProfileSyncJob job) {
        profileSyncHandler.syncOrThrow(job);
    }

    /** Same synchronous path as {@link #scheduleAfterSave}; used when lender validation must precede persist. */
    public void syncNow(ProfileSyncJob job) {
        profileSyncHandler.syncOrThrow(job);
    }
}
