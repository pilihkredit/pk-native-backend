package com.pk.infra.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.AppsFlyerCallbackRepository;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.port.ProfileAfRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppsFlyerCallbackIntakeFacade {
    private static final Logger log = LoggerFactory.getLogger(AppsFlyerCallbackIntakeFacade.class);

    private final AppsFlyerCallbackRepository appsFlyerCallbackRepository;
    private final ProfileAfRepository profileAfRepository;
    private final ObjectMapper objectMapper;

    public AppsFlyerCallbackIntakeFacade(
            AppsFlyerCallbackRepository appsFlyerCallbackRepository,
            ProfileAfRepository profileAfRepository,
            ObjectMapper objectMapper
    ) {
        this.appsFlyerCallbackRepository = appsFlyerCallbackRepository;
        this.profileAfRepository = profileAfRepository;
        this.objectMapper = objectMapper;
    }

    public IntakeResult intake(String rawPayloadJson) {
        if (rawPayloadJson == null || rawPayloadJson.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        try {
            JsonNode root = objectMapper.readTree(rawPayloadJson);
            if (root == null || !root.isObject()) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            String appsflyerId = text(root, "appsflyer_id", "appsflyerId");
            Optional<ProfileAfData> localAf = appsflyerId == null
                    ? Optional.empty()
                    : profileAfRepository.findLatestByAppsflyerId(appsflyerId);
            Long userId = localAf.map(ProfileAfData::userId).orElse(null);
            String deviceNo = localAf.map(ProfileAfData::deviceNo).filter(v -> v != null && !v.isBlank()).orElse(null);

            long id = appsFlyerCallbackRepository.insert(new AppsFlyerCallbackRepository.AppsFlyerCallbackInsert(
                    userId,
                    deviceNo,
                    appsflyerId,
                    text(root, "advertising_id", "advertisingId"),
                    text(root, "android_id", "androidId"),
                    text(root, "attributed_touch_time", "attributedTouchTime"),
                    text(root, "gp_click_time", "gpClickTime"),
                    text(root, "install_time", "installTime"),
                    text(root, "media_source", "mediaSource"),
                    text(root, "af_prt", "afPrt"),
                    text(root, "af_adset_id", "afAdsetId"),
                    text(root, "af_adset", "afAdset"),
                    text(root, "af_siteid", "afSiteid", "af_site_id"),
                    text(root, "af_c_id", "afCId"),
                    text(root, "campaign"),
                    text(root, "app_version", "appVersion"),
                    text(root, "app_id", "appId"),
                    text(root, "device_type", "deviceType"),
                    text(root, "os_version", "osVersion"),
                    text(root, "country_code", "countryCode"),
                    text(root, "city"),
                    text(root, "postal_code", "postalCode"),
                    text(root, "ip"),
                    text(root, "operator"),
                    text(root, "device_category", "deviceCategory"),
                    text(root, "platform"),
                    text(root, "device_model", "deviceModel"),
                    text(root, "idfv"),
                    text(root, "idfa"),
                    text(root, "af_ad", "afAd"),
                    text(root, "af_channel", "afChannel"),
                    text(root, "attributed_touch_type", "attributedTouchType"),
                    text(root, "af_ad_id", "afAdId"),
                    text(root, "af_ad_type", "afAdType"),
                    text(root, "contributor1_touch_type", "contributor_1_touch_type", "contributor1TouchType"),
                    text(root, "contributor1_touch_time", "contributor_1_touch_time", "contributor1TouchTime"),
                    text(root, "contributor1_af_prt", "contributor_1_af_prt", "contributor1AfPrt"),
                    text(root, "contributor1_match_type", "contributor_1_match_type", "contributor1MatchType"),
                    text(root, "contributor1_engagement_type", "contributor_1_engagement_type", "contributor1EngagementType"),
                    text(root, "bundle_id", "bundleId"),
                    text(root, "match_type", "matchType"),
                    text(root, "gp_install_begin", "gpInstallBegin"),
                    text(root, "event_source", "eventSource"),
                    text(root, "event_time", "eventTime"),
                    text(root, "event_time_selected_timezone", "eventTimeSelectedTimezone"),
                    text(root, "app_name", "appName"),
                    text(root, "campaign_type", "campaignType"),
                    text(root, "conversion_type", "conversionType"),
                    bool(root, "is_retargeting", "isRetargeting"),
                    text(root, "region"),
                    text(root, "state"),
                    text(root, "dma"),
                    bool(root, "wifi"),
                    text(root, "carrier"),
                    text(root, "language"),
                    text(root, "install_time_selected_timezone", "installTimeSelectedTimezone"),
                    text(root, "device_download_time", "deviceDownloadTime"),
                    text(root, "device_download_time_selected_timezone", "deviceDownloadTimeSelectedTimezone"),
                    text(root, "gp_referrer", "gpReferrer"),
                    text(root, "sdk_version", "sdkVersion"),
                    text(root, "api_version", "apiVersion"),
                    text(root, "user_agent", "userAgent"),
                    text(root, "selected_timezone", "selectedTimezone"),
                    text(root, "selected_currency", "selectedCurrency"),
                    text(root, "event_name", "eventName"),
                    text(root, "event_type", "eventType"),
                    text(root, "customer_user_id", "customerUserId"),
                    rawPayloadJson,
                    "processed",
                    null
            ));
            log.info(
                    "AppsFlyer callback saved: id={}, eventName={}, appsflyerId={}, userId={}, deviceNo={}",
                    id,
                    text(root, "event_name", "eventName"),
                    appsflyerId,
                    userId,
                    deviceNo
            );
            return new IntakeResult(id, "processed");
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("AppsFlyer callback intake failed: error={}", exception.getMessage(), exception);
            throw new ApiException(ApiCode.INTERNAL_SERVER_ERROR);
        }
    }

    private static String text(JsonNode root, String... fields) {
        for (String field : fields) {
            JsonNode node = root.get(field);
            if (node != null && !node.isNull()) {
                String value = node.asText();
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static Boolean bool(JsonNode root, String... fields) {
        for (String field : fields) {
            JsonNode node = root.get(field);
            if (node != null && !node.isNull()) {
                if (node.isBoolean()) {
                    return node.booleanValue();
                }
                String value = node.asText();
                if (value != null && !value.isBlank()) {
                    return Boolean.parseBoolean(value.trim());
                }
            }
        }
        return null;
    }

    public record IntakeResult(long id, String status) {
    }
}
