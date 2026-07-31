package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import com.pk.infra.ocr.OcrImageSupport;
import java.util.List;

public class IdentityVerificationCompletionService {
    private static final String MODULE_COMPLETED = "COMPLETED";

    private final ProfileIdentityRepository profileIdentityRepository;
    private final BiometricImageStore biometricImageStore;
    private final ProfileSyncOrchestrator profileSyncOrchestrator;
    private final UserDeviceWriter userDeviceWriter;
    private final ProfileVersionRepository profileVersionRepository;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final ObjectMapper objectMapper;

    public IdentityVerificationCompletionService(
            ProfileIdentityRepository profileIdentityRepository,
            BiometricImageStore biometricImageStore,
            ProfileSyncOrchestrator profileSyncOrchestrator,
            UserDeviceWriter userDeviceWriter,
            ProfileVersionRepository profileVersionRepository,
            UserProfileBindingRepository userProfileBindingRepository,
            OnboardingProgressFacade onboardingProgressFacade,
            ObjectMapper objectMapper
    ) {
        this.profileIdentityRepository = profileIdentityRepository;
        this.biometricImageStore = biometricImageStore;
        this.profileSyncOrchestrator = profileSyncOrchestrator;
        this.userDeviceWriter = userDeviceWriter;
        this.profileVersionRepository = profileVersionRepository;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.objectMapper = objectMapper;
    }

    public CompletionResult complete(IdentityVerificationCompletionCommand command) {
        String idCardRef = biometricImageStore.store(
                command.mobileNo(), BiometricImageKind.ID_CARD, command.idCardImage());
        String faceRef = biometricImageStore.store(
                command.mobileNo(), BiometricImageKind.FACE, command.faceImage());
        long profileVersionId = profileVersionRepository.createSnapshot(
                command.userId(), command.mobileNo(), List.of("identity"), "IDENTITY_OCR");
        profileIdentityRepository.upsert(new ProfileIdentityData(
                command.userId(),
                command.fullName(),
                command.encryptedIdNo(),
                command.idNoHash(),
                MODULE_COMPLETED,
                command.requestId(),
                null,
                profileVersionId,
                idCardRef,
                faceRef,
                biometricImageStore.encryptionKeyRef(),
                null,
                null,
                command.channel(),
                command.ocrVendorCallLogId()
        ));
        userDeviceWriter.upsertFromRequest(
                command.userId(), command.partnerUserId(), command.requestId(), command.device());

        ProfileSyncPayload.IdentityProfilePayload payload = buildPayload(command);
        var syncResult = profileSyncOrchestrator.scheduleAfterSave(new ProfileSyncJob(
                command.userId(),
                command.partnerUserId(),
                command.mobileNo(),
                command.requestId(),
                ProfileSyncModule.IDENTITY,
                command.device(),
                payload
        ));
        var progress = onboardingProgressFacade.getProgress(command.userId(), command.partnerUserId());
        userProfileBindingRepository.updateKycStatus(command.userId(), progress.kycStatus());
        return new CompletionResult(MODULE_COMPLETED, parseLenderResponse(syncResult.responseDataJson()));
    }

    private ProfileSyncPayload.IdentityProfilePayload buildPayload(IdentityVerificationCompletionCommand command) {
        var parsed = command.parsed();
        return new ProfileSyncPayload.IdentityProfilePayload(
                command.fullName(),
                command.plainIdNo(),
                OcrImageSupport.encodeBase64(command.faceImage()),
                command.livenessId(),
                OcrImageSupport.encodeBase64(command.idCardImage()),
                command.lenderRawOcrDetail(),
                command.channel(),
                parsed.ocrName(),
                parsed.ocrIdNo(),
                parsed.gender(),
                parsed.religion(),
                parsed.maritalStatus(),
                parsed.birthday(),
                parsed.birthPlace(),
                parsed.address(),
                parsed.occupation(),
                parsed.nationality(),
                parsed.bloodType(),
                parsed.expiryDate()
        );
    }

    private JsonNode parseLenderResponse(String responseJson) {
        if (responseJson == null || responseJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(responseJson);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    public record CompletionResult(String moduleStatus, JsonNode lenderResponse) {
    }
}
