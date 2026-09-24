package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.profile.FaceComparisonBaselineResolver;
import java.time.Instant;
import java.util.Objects;

/**
 * Evaluates whether device-switch face is required and validates/consumes face tickets at login.
 */
public class LoginDeviceSwitchGateService {
    private final UserAuthRepository userAuthRepository;
    private final FaceComparisonBaselineResolver baselineResolver;
    private final DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository;

    public LoginDeviceSwitchGateService(
            UserAuthRepository userAuthRepository,
            FaceComparisonBaselineResolver baselineResolver,
            DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository
    ) {
        this.userAuthRepository = userAuthRepository;
        this.baselineResolver = baselineResolver;
        this.deviceSwitchFaceRepository = deviceSwitchFaceRepository;
    }

    public boolean evaluateFaceRequiredForMobileCheck(long userId, String deviceNo) {
        return isFaceRequired(userId, deviceNo);
    }

    public void assertLoginAllowed(long userId, String deviceNo, String faceVerifyToken) {
        if (!isFaceRequired(userId, deviceNo)) {
            return;
        }
        String token = normalizeToken(faceVerifyToken);
        if (token == null) {
            throw new ApiException(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
        }
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        deviceSwitchFaceRepository.findByToken(token)
                .filter(value -> value.userId() == userId)
                .filter(value -> Objects.equals(value.deviceNo(), normalizedDevice))
                .filter(value -> value.usableAt(Instant.now()))
                .orElseThrow(() -> new ApiException(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED));
    }

    /** Consumes face ticket before opening session; no-op when face is not required. */
    public void consumeFaceTokenIfRequired(long userId, String deviceNo, String faceVerifyToken) {
        if (!isFaceRequired(userId, deviceNo)) {
            return;
        }
        String token = normalizeToken(faceVerifyToken);
        if (token == null) {
            throw new ApiException(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
        }
        if (!deviceSwitchFaceRepository.consume(token, Instant.now())) {
            throw new ApiException(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
        }
    }

    public void updateLastLoginDeviceNo(long userId, String deviceNo) {
        userAuthRepository.updateLastLoginDeviceNo(userId, AuthDeviceNoNormalizer.requireNonBlank(deviceNo));
    }

    private boolean isFaceRequired(long userId, String deviceNo) {
        if (!baselineResolver.hasComparableBaseline(userId)) {
            return false;
        }
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        String lastDevice = userAuthRepository.findLastLoginDeviceNo(userId)
                .map(AuthDeviceNoNormalizer::normalize)
                .orElse(null);
        if (lastDevice == null || lastDevice.isEmpty()) {
            return true;
        }
        return !lastDevice.equals(normalizedDevice);
    }

    private static String normalizeToken(String faceVerifyToken) {
        if (faceVerifyToken == null || faceVerifyToken.isBlank()) {
            return null;
        }
        return faceVerifyToken.trim();
    }
}
