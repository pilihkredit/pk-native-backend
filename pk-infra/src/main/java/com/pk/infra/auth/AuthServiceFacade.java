package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthSession;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.OtpChallenge;
import com.pk.core.auth.PasswordFormatValidator;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.PasswordHasher;
import com.pk.core.auth.port.UserPasswordCredentialRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class AuthServiceFacade {
    private static final String LOGIN_CHANNEL_OTP = "OTP";
    private static final String LOGIN_CHANNEL_PASSWORD = "PASSWORD";
    private static final String SMS_PURPOSE_OTP = "OTP";

    private final AuthProperties authProperties;
    private final SessionStore sessionStore;
    private final OtpChallengeStore otpChallengeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenIssuer tokenIssuer;
    private final UserAuthRepository userAuthRepository;
    private final UserPasswordCredentialRepository userPasswordCredentialRepository;
    private final PasswordHasher passwordHasher;
    private final SmsSendLogRepository smsSendLogRepository;
    private final SmsSender smsSender;

    public AuthServiceFacade(
            AuthProperties authProperties,
            SessionStore sessionStore,
            OtpChallengeStore otpChallengeStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer,
            UserAuthRepository userAuthRepository,
            UserPasswordCredentialRepository userPasswordCredentialRepository,
            PasswordHasher passwordHasher,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender
    ) {
        this.authProperties = authProperties;
        this.sessionStore = sessionStore;
        this.otpChallengeStore = otpChallengeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenIssuer = tokenIssuer;
        this.userAuthRepository = userAuthRepository;
        this.userPasswordCredentialRepository = userPasswordCredentialRepository;
        this.passwordHasher = passwordHasher;
        this.smsSendLogRepository = smsSendLogRepository;
        this.smsSender = smsSender;
    }

    public OtpSendResult sendOtp(String mobileNo, String deviceNo) {
        validateMobile(mobileNo);
        Objects.requireNonNull(deviceNo, "deviceNo is required");
        if (deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        Optional<Duration> wait = otpChallengeStore.timeUntilResendAllowed(deviceNo);
        if (wait.isPresent()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
        enforceDailySmsLimit(mobileNo);

        String otpToken = OtpCodeGenerator.token();
        String otpCode = OtpCodeGenerator.sixDigits();
        Instant expiresAt = Instant.now().plus(authProperties.otpTtl());
        otpChallengeStore.save(
                otpToken,
                new OtpChallenge(mobileNo, deviceNo, otpCode, expiresAt),
                authProperties.otpTtl()
        );

        Optional<Long> profileId = userAuthRepository.findByMobileNo(mobileNo).map(UserProfileSummary::profileId);
        long logId = smsSendLogRepository.insert(new SmsSendLogRepository.SmsSendLogEntry(
                profileId,
                mobileNo,
                deviceNo,
                otpCode,
                SMS_PURPOSE_OTP
        ));

        SmsSendResult smsResult = smsSender.send(mobileNo, otpCode);
        smsSendLogRepository.updateProviderResult(logId, smsResult);
        if (!smsResult.success()) {
            otpChallengeStore.delete(otpToken);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }

        otpChallengeStore.markSent(deviceNo, authProperties.otpResendInterval());
        return new OtpSendResult(
                otpToken,
                authProperties.otpTtl().toSeconds(),
                authProperties.otpResendInterval().toSeconds(),
                otpCode
        );
    }

    private void enforceDailySmsLimit(String mobileNo) {
        Instant startOfDay = ZonedDateTime.now(authProperties.otpDailyLimitZone())
                .toLocalDate()
                .atStartOfDay(authProperties.otpDailyLimitZone())
                .toInstant();
        long sentToday = smsSendLogRepository.countSince(mobileNo, startOfDay);
        if (sentToday >= authProperties.otpDailyLimit()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    public MobileCheckResult checkMobileRegistration(String mobileNo, String deviceNo) {
        validateMobile(mobileNo);
        Objects.requireNonNull(deviceNo, "deviceNo is required");
        if (deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        boolean registered = userAuthRepository.findByMobileNo(mobileNo).isPresent();
        boolean passwordSet = userAuthRepository.findByMobileNo(mobileNo)
                .map(profile -> userPasswordCredentialRepository.isPasswordSet(profile.profileId()))
                .orElse(false);
        return new MobileCheckResult(registered, registered ? "EXISTING" : "NEW", passwordSet);
    }

    public void setPassword(long profileId, String password, String confirmPassword) {
        if (password == null || password.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!password.equals(confirmPassword)) {
            throw new ApiException(ApiCode.PASSWORD_CONFIRM_MISMATCH);
        }
        if (!PasswordFormatValidator.isValid(password)) {
            throw new ApiException(ApiCode.INVALID_PASSWORD_FORMAT);
        }
        if (userPasswordCredentialRepository.isPasswordSet(profileId)) {
            throw new ApiException(ApiCode.PASSWORD_ALREADY_SET);
        }
        userPasswordCredentialRepository.insert(profileId, passwordHasher.hash(password));
    }

    public PasswordLoginResult loginByPassword(String mobileNo, String password, String deviceNo) {
        validateMobile(mobileNo);
        if (password == null || password.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        UserProfileSummary profile = userAuthRepository.findByMobileNo(mobileNo)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_MOBILE_OR_PASSWORD));
        UserPasswordCredentialRepository.PasswordCredential credential = userPasswordCredentialRepository
                .findByProfileId(profile.profileId())
                .orElseThrow(() -> new ApiException(ApiCode.PASSWORD_NOT_SET));

        Instant now = Instant.now();
        if (credential.lockedUntil() != null && credential.lockedUntil().isAfter(now)) {
            throw new ApiException(ApiCode.PASSWORD_ACCOUNT_LOCKED);
        }

        if (!passwordHasher.matches(password, credential.passwordHash())) {
            int nextAttempts = credential.failedAttempts() + 1;
            if (nextAttempts >= authProperties.passwordMaxFailedAttempts()) {
                userPasswordCredentialRepository.recordFailedAttempt(
                        profile.profileId(),
                        0,
                        now.plus(authProperties.passwordLockDuration())
                );
            } else {
                userPasswordCredentialRepository.recordFailedAttempt(profile.profileId(), nextAttempts, null);
            }
            throw new ApiException(ApiCode.INVALID_MOBILE_OR_PASSWORD);
        }

        userPasswordCredentialRepository.resetFailedAttempts(profile.profileId());
        TokenPair tokenPair = openSession(profile, deviceNo, LOGIN_CHANNEL_PASSWORD);
        return new PasswordLoginResult(profile, tokenPair, true);
    }

    public boolean isPasswordSet(long profileId) {
        return userPasswordCredentialRepository.isPasswordSet(profileId);
    }

    public OtpVerifyResult verifyOtp(String mobileNo, String otpToken, String otpCode, String deviceNo) {
        validateMobile(mobileNo);
        if (otpToken == null || otpToken.isBlank() || otpCode == null || otpCode.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        OtpChallenge challenge = otpChallengeStore.findByToken(otpToken)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));
        if (challenge.expired(Instant.now())) {
            otpChallengeStore.delete(otpToken);
            throw new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
        }
        if (!mobileNo.equals(challenge.mobileNo())) {
            throw new ApiException(ApiCode.OTP_TOKEN_MISMATCH);
        }
        if (!deviceNo.equals(challenge.deviceNo())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!otpCode.equals(challenge.otpCode())) {
            throw new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
        }
        otpChallengeStore.delete(otpToken);

        UserProfileSummary profile = userAuthRepository.findByMobileNo(mobileNo)
                .orElseGet(() -> userAuthRepository.createByMobileNo(mobileNo));
        TokenPair tokenPair = openSession(profile, deviceNo, LOGIN_CHANNEL_OTP);
        boolean passwordSet = userPasswordCredentialRepository.isPasswordSet(profile.profileId());
        return new OtpVerifyResult(profile, tokenPair, passwordSet);
    }

    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        RefreshTokenStore.RefreshTokenRecord record = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        AuthSession session = sessionStore.findByProfileId(record.profileId())
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (session.sessionVersion() != record.sessionVersion()) {
            refreshTokenStore.delete(refreshToken);
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        UserProfileSummary profile = userAuthRepository.findByProfileId(record.profileId())
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        return issueAccessToken(profile, session.sessionVersion(), session.deviceId());
    }

    public void logout(AuthenticatedPrincipal principal) {
        sessionStore.delete(principal.profileId());
        refreshTokenStore.deleteAllForProfile(principal.profileId());
    }

    public AuthenticatedPrincipal validateAccessToken(String accessToken) {
        AuthenticatedPrincipal principal = tokenIssuer.parseAccessToken(accessToken);
        AuthSession session = sessionStore.findByProfileId(principal.profileId())
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (session.sessionVersion() != principal.sessionVersion()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return principal;
    }

    private TokenPair openSession(UserProfileSummary profile, String deviceId, String loginChannel) {
        long nextVersion = sessionStore.findByProfileId(profile.profileId())
                .map(AuthSession::sessionVersion)
                .orElse(0L) + 1L;
        Instant issuedAt = Instant.now();
        AuthSession session = new AuthSession(profile.profileId(), nextVersion, deviceId, loginChannel, issuedAt);
        sessionStore.save(profile.profileId(), session, authProperties.refreshTokenTtl());
        refreshTokenStore.deleteAllForProfile(profile.profileId());
        TokenPair tokenPair = tokenIssuer.issue(
                profile.profileId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                nextVersion,
                deviceId
        );
        refreshTokenStore.save(
                tokenPair.refreshToken(),
                new RefreshTokenStore.RefreshTokenRecord(profile.profileId(), nextVersion, deviceId),
                authProperties.refreshTokenTtl()
        );
        return tokenPair;
    }

    private TokenPair issueAccessToken(UserProfileSummary profile, long sessionVersion, String deviceId) {
        return tokenIssuer.issue(
                profile.profileId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                sessionVersion,
                deviceId
        );
    }

    private void validateMobile(String mobileNo) {
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    public record OtpSendResult(String otpToken, long expireIn, long resendAfter, String otpCodeForLocalDev) {
    }

    public record MobileCheckResult(boolean registered, String accountStatus, boolean passwordSet) {
    }

    public record OtpVerifyResult(UserProfileSummary profile, TokenPair tokenPair, boolean passwordSet) {
    }

    public record PasswordLoginResult(UserProfileSummary profile, TokenPair tokenPair, boolean passwordSet) {
    }
}
