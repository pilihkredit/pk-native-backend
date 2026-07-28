package com.pk.infra.profile;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Optional;

/**
 * Builds lender appsFlyerInstall payload from user_profile_af,
 * filling blank fields from appsflyer_callback (prefer install, else latest).
 */
public class AppsFlyerLenderPayloadResolver {
    private final AppsFlyerCallbackRepository appsFlyerCallbackRepository;

    public AppsFlyerLenderPayloadResolver(AppsFlyerCallbackRepository appsFlyerCallbackRepository) {
        this.appsFlyerCallbackRepository = appsFlyerCallbackRepository;
    }

    public ProfileSyncPayload.AppsFlyerInstallPayload resolve(ProfileAfData af) {
        Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> callback = Optional.empty();
        if (af.appsflyerId() != null && !af.appsflyerId().isBlank()) {
            String afId = af.appsflyerId().trim();
            callback = appsFlyerCallbackRepository.findLatestByAppsflyerIdAndEventName(afId, "install")
                    .or(() -> appsFlyerCallbackRepository.findLatestByAppsflyerId(afId));
        }
        return AppsFlyerPayloadMapper.toPayload(af, callback.orElse(null));
    }
}
