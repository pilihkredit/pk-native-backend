package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.profile.AppsFlyerLenderPayloadResolver;
import com.pk.infra.profile.ProfileSyncJob;
import com.pk.infra.profile.ProfileSyncOrchestrator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreditAppsFlyerPreSyncTest {
    @Test
    void upsertsAppsFlyerBeforeCreditWhenInstallCallbackExists() {
        AppsFlyerLenderPayloadResolver resolver = mock(AppsFlyerLenderPayloadResolver.class);
        ProfileSyncOrchestrator orchestrator = mock(ProfileSyncOrchestrator.class);
        CreditApplicationRepository creditApplicationRepository = mock(CreditApplicationRepository.class);
        LenderDeviceContext device = mock(LenderDeviceContext.class);
        when(device.deviceNo()).thenReturn("dev-1");
        ProfileSyncPayload.AppsFlyerInstallPayload payload = samplePayload();
        when(resolver.resolveInstallByDeviceNo("dev-1")).thenReturn(Optional.of(payload));
        when(creditApplicationRepository.findById(9L)).thenReturn(Optional.of(sampleApplication()));

        new CreditAppsFlyerPreSync(resolver, orchestrator, creditApplicationRepository)
                .syncBeforeCreditApply(sampleJob(device));

        ArgumentCaptor<ProfileSyncJob> captor = ArgumentCaptor.forClass(ProfileSyncJob.class);
        verify(orchestrator).scheduleAfterSave(captor.capture());
        ProfileSyncJob job = captor.getValue();
        assertThat(job.module()).isEqualTo(ProfileSyncModule.APPSFLYER_INSTALL);
        assertThat(job.userId()).isEqualTo(42L);
        assertThat(job.requestId()).isEqualTo("APPLY-1");
        assertThat(job.device()).isSameAs(device);
        assertThat(job.payloadSnapshot()).isSameAs(payload);
    }

    @Test
    void skipsWhenAppsFlyerMissingAndDoesNotThrow() {
        AppsFlyerLenderPayloadResolver resolver = mock(AppsFlyerLenderPayloadResolver.class);
        ProfileSyncOrchestrator orchestrator = mock(ProfileSyncOrchestrator.class);
        CreditApplicationRepository creditApplicationRepository = mock(CreditApplicationRepository.class);
        LenderDeviceContext device = mock(LenderDeviceContext.class);
        when(device.deviceNo()).thenReturn("dev-missing");
        when(resolver.resolveInstallByDeviceNo("dev-missing")).thenReturn(Optional.empty());

        new CreditAppsFlyerPreSync(resolver, orchestrator, creditApplicationRepository)
                .syncBeforeCreditApply(sampleJob(device));
        verify(orchestrator, never()).scheduleAfterSave(any());
        verify(creditApplicationRepository, never()).findById(any(Long.class));
    }

    @Test
    void continuesCreditWhenUpsertFails() {
        AppsFlyerLenderPayloadResolver resolver = mock(AppsFlyerLenderPayloadResolver.class);
        ProfileSyncOrchestrator orchestrator = mock(ProfileSyncOrchestrator.class);
        CreditApplicationRepository creditApplicationRepository = mock(CreditApplicationRepository.class);
        LenderDeviceContext device = mock(LenderDeviceContext.class);
        when(device.deviceNo()).thenReturn("dev-1");
        when(resolver.resolveInstallByDeviceNo("dev-1")).thenReturn(Optional.of(samplePayload()));
        when(creditApplicationRepository.findById(9L)).thenReturn(Optional.of(sampleApplication()));
        when(orchestrator.scheduleAfterSave(any())).thenThrow(new ApiException(ApiCode.SERVICE_UNAVAILABLE));

        new CreditAppsFlyerPreSync(resolver, orchestrator, creditApplicationRepository)
                .syncBeforeCreditApply(sampleJob(device));
        verify(orchestrator).scheduleAfterSave(any());
    }

    private static CreditApplyJob sampleJob(LenderDeviceContext device) {
        return new CreditApplyJob(
                9L,
                "APPLY-1",
                "partner-1",
                "81234567890",
                BigDecimal.ONE,
                BigDecimal.TEN,
                "1.1.1.1",
                "Jakarta",
                device,
                List.of()
        );
    }

    private static CreditApplicationRepository.CreditApplicationRecord sampleApplication() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                9L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                42L,
                "partner-1",
                null
        );
    }

    private static ProfileSyncPayload.AppsFlyerInstallPayload samplePayload() {
        return new ProfileSyncPayload.AppsFlyerInstallPayload(
                "af-1", null, null, null, null, null, "organic", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }
}
