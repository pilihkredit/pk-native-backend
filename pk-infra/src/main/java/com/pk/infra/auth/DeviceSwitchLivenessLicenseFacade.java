package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.profile.TrustDecisionIdentityFacade;

/**
 * Unauthenticated TrustDecision liveness license for device-switch login face flow.
 */
public class DeviceSwitchLivenessLicenseFacade {
    private final UserAuthRepository userAuthRepository;
    private final LoginDeviceSwitchGateService loginDeviceSwitchGateService;
    private final TrustDecisionIdentityFacade trustDecisionIdentityFacade;
    private final DeviceSwitchLivenessLicenseRateLimiter rateLimiter;
    private final DeviceSwitchSecurityConfigLoader configLoader;

    public DeviceSwitchLivenessLicenseFacade(
            UserAuthRepository userAuthRepository,
            LoginDeviceSwitchGateService loginDeviceSwitchGateService,
            TrustDecisionIdentityFacade trustDecisionIdentityFacade,
            DeviceSwitchLivenessLicenseRateLimiter rateLimiter,
            DeviceSwitchSecurityConfigLoader configLoader
    ) {
        this.userAuthRepository = userAuthRepository;
        this.loginDeviceSwitchGateService = loginDeviceSwitchGateService;
        this.trustDecisionIdentityFacade = trustDecisionIdentityFacade;
        this.rateLimiter = rateLimiter;
        this.configLoader = configLoader;
    }

    public TrustDecisionIdentityFacade.LivenessLicenseResult obtainLicense(
            String mobileNo,
            String deviceNo,
            Integer sessionDurationSeconds,
            String clientIp,
            String clientRequestId,
            String traceId
    ) {
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        DeviceSwitchSecurityConfigLoader.Settings settings = configLoader.loadSettings();
        String normalizedDevice = requireDeviceNoWithinLimit(deviceNo, settings.deviceNoMaxLength());
        UserProfileSummary profile = userAuthRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS));
        if (!loginDeviceSwitchGateService.evaluateFaceRequiredForMobileCheck(
                profile.userId(),
                normalizedDevice
        )) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device-switch face verification is not required");
        }
        int duration = sessionDurationSeconds == null
                ? settings.livenessSessionDurationSeconds()
                : sessionDurationSeconds;
        String normalizedIp = DeviceSwitchLivenessLicenseRateLimiter.normalizeClientIp(clientIp);
        rateLimiter.checkAndRecord(normalizedIp, normalizedDevice);
        return trustDecisionIdentityFacade.obtainLivenessLicense(
                profile.userId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                duration,
                clientRequestId,
                traceId
        );
    }

    private static String requireDeviceNoWithinLimit(String deviceNo, int maxLength) {
        String normalized = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        if (normalized.length() > maxLength) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "deviceNo exceeds the allowed length " + maxLength);
        }
        return normalized;
    }
}
