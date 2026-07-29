package com.pk.infra.profile;

import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Optional;

/**
 * Builds lender appsFlyerInstall payload from user_profile_af,
 * filling blank fields from appsflyer_callback (prefer install, else latest).
 * Lookup order: appsflyer_id → advertising_id → android_id → device_no.
 */
public class AppsFlyerLenderPayloadResolver {
    private final AppsFlyerCallbackRepository appsFlyerCallbackRepository;

    public AppsFlyerLenderPayloadResolver(AppsFlyerCallbackRepository appsFlyerCallbackRepository) {
        this.appsFlyerCallbackRepository = appsFlyerCallbackRepository;
    }

    public ProfileSyncPayload.AppsFlyerInstallPayload resolve(ProfileAfData af) {
        Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> callback = findCallback(af);
        return AppsFlyerPayloadMapper.toPayload(af, callback.orElse(null));
    }

    private Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> findCallback(ProfileAfData af) {
        return findByAppsflyerId(af.appsflyerId())
                .or(() -> findByAdvertisingId(af.advertisingId()))
                .or(() -> findByAndroidId(af.androidId()))
                .or(() -> findByDeviceNo(af.deviceNo()));
    }

    private Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> findByAppsflyerId(String appsflyerId) {
        if (blank(appsflyerId)) {
            return Optional.empty();
        }
        String id = appsflyerId.trim();
        return appsFlyerCallbackRepository.findLatestByAppsflyerIdAndEventName(id, "install")
                .or(() -> appsFlyerCallbackRepository.findLatestByAppsflyerId(id));
    }

    private Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> findByAdvertisingId(String advertisingId) {
        if (blank(advertisingId)) {
            return Optional.empty();
        }
        String id = advertisingId.trim();
        return appsFlyerCallbackRepository.findLatestByAdvertisingIdAndEventName(id, "install")
                .or(() -> appsFlyerCallbackRepository.findLatestByAdvertisingId(id));
    }

    private Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> findByAndroidId(String androidId) {
        if (blank(androidId)) {
            return Optional.empty();
        }
        String id = androidId.trim();
        return appsFlyerCallbackRepository.findLatestByAndroidIdAndEventName(id, "install")
                .or(() -> appsFlyerCallbackRepository.findLatestByAndroidId(id));
    }

    private Optional<AppsFlyerCallbackRepository.AppsFlyerCallbackData> findByDeviceNo(String deviceNo) {
        if (blank(deviceNo)) {
            return Optional.empty();
        }
        String id = deviceNo.trim();
        return appsFlyerCallbackRepository.findLatestByDeviceNoAndEventName(id, "install")
                .or(() -> appsFlyerCallbackRepository.findLatestByDeviceNo(id));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
