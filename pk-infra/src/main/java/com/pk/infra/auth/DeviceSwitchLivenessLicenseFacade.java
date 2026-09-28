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

    public DeviceSwitchLivenessLicenseFacade(
            UserAuthRepository userAuthRepository,
            LoginDeviceSwitchGateService loginDeviceSwitchGateService,
            TrustDecisionIdentityFacade trustDecisionIdentityFacade,
            DeviceSwitchLivenessLicenseRateLimiter rateLimiter
    ) {
        this.userAuthRepository = userAuthRepository;
        this.loginDeviceSwitchGateService = loginDeviceSwitchGateService;
        this.trustDecisionIdentityFacade = trustDecisionIdentityFacade;
        this.rateLimiter = rateLimiter;
    }

    public TrustDecisionIdentityFacade.LivenessLicenseResult obtainLicense(
            String mobileNo,
            String deviceNo,
            int sessionDurationSeconds,
            String clientIp,
            String clientRequestId,
            String traceId
    ) {
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
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
        String normalizedIp = DeviceSwitchLivenessLicenseRateLimiter.normalizeClientIp(clientIp);
        rateLimiter.assertAllowed(normalizedIp, normalizedDevice);
        rateLimiter.recordInvocation(normalizedIp, normalizedDevice);
        return trustDecisionIdentityFacade.obtainLivenessLicense(
                profile.userId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                sessionDurationSeconds,
                clientRequestId,
                traceId
        );
    }
}
