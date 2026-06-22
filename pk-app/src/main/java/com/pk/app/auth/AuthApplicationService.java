package com.pk.app.auth;

import com.pk.core.auth.AccountStatus;
import com.pk.core.auth.AuthAction;
import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import com.pk.core.validation.MobileNumberValidator;
import com.pk.infra.user.UserProfileRecord;
import com.pk.infra.user.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {
    private final UserProfileRepository userProfileRepository;
    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;

    public AuthApplicationService(
            UserProfileRepository userProfileRepository,
            OtpService otpService,
            JwtTokenService jwtTokenService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.otpService = otpService;
        this.jwtTokenService = jwtTokenService;
    }

    public MobileCheckResult checkMobile(String mobileNo, String deviceNo) {
        validateMobileAndDevice(mobileNo, deviceNo);
        otpService.enforceSendRateLimit(mobileNo, deviceNo);
        boolean registered = userProfileRepository.findActiveByMobileNo(mobileNo).isPresent();
        AccountStatus status = registered ? AccountStatus.EXISTING : AccountStatus.NEW;
        return new MobileCheckResult(registered, status.name());
    }

    public OtpService.OtpSendResult sendOtp(String mobileNo, String deviceNo) {
        validateMobileAndDevice(mobileNo, deviceNo);
        return otpService.send(mobileNo, deviceNo);
    }

    public VerifyOtpResult verifyOtp(
            String mobileNo,
            String deviceNo,
            String otpToken,
            String otpCode
    ) {
        validateMobileAndDevice(mobileNo, deviceNo);
        otpService.verify(mobileNo, deviceNo, otpToken, otpCode);

        var existing = userProfileRepository.findActiveByMobileNo(mobileNo);
        boolean newUser;
        UserProfileRecord profile;
        AuthAction authAction;
        if (existing.isPresent()) {
            profile = existing.get();
            newUser = false;
            authAction = AuthAction.LOGIN;
        } else {
            profile = userProfileRepository.insertNewUser(mobileNo);
            newUser = true;
            authAction = AuthAction.REGISTER;
        }

        var issued = jwtTokenService.issueToken(profile.id(), profile.partnerUserId(), profile.mobileNo());
        var user = new com.pk.app.web.AuthenticatedUser(
                profile.id(),
                profile.partnerUserId(),
                profile.mobileNo(),
                profile.kycStatus()
        );
        return new VerifyOtpResult(
                profile.partnerUserId(),
                issued.accessToken(),
                "Bearer",
                issued.expiresInSeconds(),
                authAction.name(),
                newUser,
                user.resolveUserStage().name()
        );
    }

    private static void validateMobileAndDevice(String mobileNo, String deviceNo) {
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new AppBusinessException(AppErrorCodes.DEVICE_NO_REQUIRED);
        }
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new AppBusinessException(AppErrorCodes.INVALID_REQUEST);
        }
    }

    public record MobileCheckResult(boolean registered, String accountStatus) {
    }

    public record VerifyOtpResult(
            String partnerUserId,
            String accessToken,
            String tokenType,
            long expiresIn,
            String authAction,
            boolean newUser,
            String userStage
    ) {
    }
}
