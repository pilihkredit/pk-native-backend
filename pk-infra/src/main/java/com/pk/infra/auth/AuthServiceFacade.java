package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
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
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.core.auth.port.WhatsAppSender;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

public class AuthServiceFacade {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceFacade.class);
    private static final String LOGIN_CHANNEL_OTP = "OTP";
    private static final String LOGIN_CHANNEL_MOBILE_CHANGE = "MOBILE_CHANGE";
    private static final String LOGIN_CHANNEL_WHATSAPP = "WHATSAPP";
    private static final String LOGIN_CHANNEL_PASSWORD = "PASSWORD";
    private static final String OTP_PURPOSE = "OTP";

    private final AuthProperties authProperties;
    private final AuthOtpConfigLoader authOtpConfigLoader;
    private final SmsConfigLoader smsConfigLoader;
    private final SessionStore sessionStore;
    private final OtpChallengeStore otpChallengeStore;
    private final OtpChallengeStore whatsappOtpChallengeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenIssuer tokenIssuer;
    private final UserAuthRepository userAuthRepository;
    private final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private final SmsSendLogRepository smsSendLogRepository;
    private final SmsSender smsSender;
    private final WhatsAppSendLogRepository whatsAppSendLogRepository;
    private final WhatsAppSender whatsAppSender;
    private final WhatsAppConfigLoader whatsAppConfigLoader;
    private final UserProfileBindingRepository userProfileBindingRepository;
    private final AppsFlyerS2sReporter appsFlyerS2sReporter;

    public AuthServiceFacade(
            AuthProperties authProperties,
            AuthOtpConfigLoader authOtpConfigLoader,
            SmsConfigLoader smsConfigLoader,
            SessionStore sessionStore,
            @Qualifier("otpChallengeStore") OtpChallengeStore otpChallengeStore,
            @Qualifier("whatsappOtpChallengeStore") OtpChallengeStore whatsappOtpChallengeStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer,
            UserAuthRepository userAuthRepository,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender,
            WhatsAppSendLogRepository whatsAppSendLogRepository,
            WhatsAppSender whatsAppSender,
            WhatsAppConfigLoader whatsAppConfigLoader,
            UserProfileBindingRepository userProfileBindingRepository,
            AppsFlyerS2sReporter appsFlyerS2sReporter
    ) {
        this.authProperties = authProperties;
        this.authOtpConfigLoader = authOtpConfigLoader;
        this.smsConfigLoader = smsConfigLoader;
        this.sessionStore = sessionStore;
        this.otpChallengeStore = otpChallengeStore;
        this.whatsappOtpChallengeStore = whatsappOtpChallengeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenIssuer = tokenIssuer;
        this.userAuthRepository = userAuthRepository;
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.smsSendLogRepository = smsSendLogRepository;
        this.smsSender = smsSender;
        this.whatsAppSendLogRepository = whatsAppSendLogRepository;
        this.whatsAppSender = whatsAppSender;
        this.whatsAppConfigLoader = whatsAppConfigLoader;
        this.userProfileBindingRepository = userProfileBindingRepository;
        this.appsFlyerS2sReporter = appsFlyerS2sReporter;
    }

    public OtpSendResult sendOtp(String mobileNo, String deviceNo) {
        validateMobile(mobileNo);
        requireDeviceNo(deviceNo);
        AuthOtpConfigLoader.AuthOtpConfig otpConfig = authOtpConfigLoader.load();
        enforceResendInterval(otpChallengeStore, deviceNo);
        enforceDailyLimit(
                smsSendLogRepository::countSince,
                mobileNo,
                otpConfig.otpDailyLimit(),
                otpConfig.otpDailyLimitZone()
        );

        String otpToken = OtpCodeGenerator.token();
        SmsConfigLoader.SmsConf smsConf = smsConfigLoader.loadConf();
        String otpCode = OtpCodeGenerator.numericCode(smsConf.codeLength());
        Duration otpTtl = Duration.ofSeconds(smsConf.expireTimeSeconds());
        Instant expiresAt = Instant.now().plus(otpTtl);
        otpChallengeStore.save(
                otpToken,
                new OtpChallenge(mobileNo, deviceNo, otpCode, expiresAt),
                otpTtl
        );

        Optional<Long> userId = userAuthRepository.findByMobileNo(mobileNo).map(UserProfileSummary::userId);
        long logId = smsSendLogRepository.insert(new SmsSendLogRepository.SmsSendLogEntry(
                userId,
                mobileNo,
                deviceNo,
                otpToken,
                otpCode,
                OTP_PURPOSE
        ));

        SmsSendResult smsResult = smsSender.send(mobileNo, otpCode);
        smsSendLogRepository.updateProviderResult(logId, smsResult);
        if (!smsResult.success()) {
            otpChallengeStore.delete(otpToken);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }

        otpChallengeStore.markSent(deviceNo, otpConfig.otpResendInterval());
        return new OtpSendResult(
                otpToken,
                otpTtl.toSeconds(),
                otpConfig.otpResendInterval().toSeconds(),
                otpCode
        );
    }

    public OtpSendResult sendWhatsAppCode(String mobileNo, String deviceNo) {
        validateMobile(mobileNo);
        requireDeviceNo(deviceNo);
        WhatsAppConfigLoader.WhatsAppConf whatsAppConf = whatsAppConfigLoader.loadConf();
        AuthOtpConfigLoader.AuthOtpConfig otpConfig = authOtpConfigLoader.load();
        enforceResendInterval(whatsappOtpChallengeStore, deviceNo);
        enforceDailyLimit(
                whatsAppSendLogRepository::countSince,
                mobileNo,
                whatsAppConfigLoader.loadDailyLimit(),
                otpConfig.otpDailyLimitZone()
        );

        String otpToken = OtpCodeGenerator.token();
        String otpCode = OtpCodeGenerator.sixDigits();
        Duration challengeTtl = whatsAppConf.expireTime();
        Instant expiresAt = Instant.now().plus(challengeTtl);
        whatsappOtpChallengeStore.save(
                otpToken,
                new OtpChallenge(mobileNo, deviceNo, otpCode, expiresAt),
                challengeTtl
        );

        Optional<Long> userId = userAuthRepository.findByMobileNo(mobileNo).map(UserProfileSummary::userId);
        long logId = whatsAppSendLogRepository.insert(new WhatsAppSendLogRepository.WhatsAppSendLogEntry(
                userId,
                mobileNo,
                deviceNo,
                otpToken,
                otpCode,
                OTP_PURPOSE
        ));

        SmsSendResult sendResult = whatsAppSender.send(mobileNo, otpCode);
        whatsAppSendLogRepository.updateProviderResult(logId, sendResult);
        if (!sendResult.success()) {
            whatsappOtpChallengeStore.delete(otpToken);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }

        Duration resendInterval = whatsAppConf.minInterval();
        whatsappOtpChallengeStore.markSent(deviceNo, resendInterval);
        return new OtpSendResult(
                otpToken,
                challengeTtl.toSeconds(),
                resendInterval.toSeconds(),
                otpCode
        );
    }

    public MobileCheckResult checkMobileRegistration(String mobileNo, String deviceNo) {
        validateMobile(mobileNo);
        requireDeviceNo(deviceNo);

        boolean registered = userAuthRepository.findByMobileNo(mobileNo).isPresent();
        boolean passwordSet = userAuthRepository.findByMobileNo(mobileNo)
                .map(profile -> userAuthRepository.isPasswordSet(profile.userId()))
                .orElse(false);
        return new MobileCheckResult(registered, registered ? "EXISTING" : "NEW", passwordSet);
    }

    public void setPassword(long userId, String password, String confirmPassword) {
        if (password == null || password.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (!password.equals(confirmPassword)) {
            throw new ApiException(ApiCode.PASSWORD_CONFIRM_MISMATCH);
        }
        if (!PasswordFormatValidator.isValid(password)) {
            throw new ApiException(ApiCode.INVALID_PASSWORD_FORMAT);
        }
        if (userAuthRepository.isPasswordSet(userId)) {
            throw new ApiException(ApiCode.PASSWORD_ALREADY_SET);
        }
        EncryptedField encryptedPassword = sensitiveFieldEncryptor.encrypt(password);
        userAuthRepository.savePassword(userId, encryptedPassword);
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
        UserAuthRepository.PasswordCredential credential = userAuthRepository
                .findPasswordCredential(profile.userId())
                .orElseThrow(() -> new ApiException(ApiCode.PASSWORD_NOT_SET));

        if (!passwordMatches(password, credential.password())) {
            throw new ApiException(ApiCode.INVALID_MOBILE_OR_PASSWORD);
        }

        TokenPair tokenPair = openSession(profile, deviceNo, LOGIN_CHANNEL_PASSWORD);
        return new PasswordLoginResult(profile, tokenPair, true);
    }

    public boolean isPasswordSet(long userId) {
        return userAuthRepository.isPasswordSet(userId);
    }

    public OtpVerifyResult verifyOtp(String mobileNo, String otpToken, String otpCode, String deviceNo) {
        return verifyOtp(mobileNo, otpToken, otpCode, deviceNo, null);
    }

    public OtpVerifyResult verifyOtp(
            String mobileNo,
            String otpToken,
            String otpCode,
            String deviceNo,
            String systemPlatform
    ) {
        return verifyChallengeAndLogin(
                otpChallengeStore,
                mobileNo,
                otpToken,
                otpCode,
                deviceNo,
                LOGIN_CHANNEL_OTP,
                systemPlatform
        );
    }

    public OtpVerifyResult loginWithWhatsApp(String mobileNo, String otpCode, String deviceNo) {
        return loginWithWhatsApp(mobileNo, otpCode, deviceNo, null);
    }

    public OtpVerifyResult loginWithWhatsApp(
            String mobileNo,
            String otpCode,
            String deviceNo,
            String systemPlatform
    ) {
        validateMobile(mobileNo);
        if (otpCode == null || otpCode.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (isWhatsAppConfiguredDefaultCode(mobileNo, otpCode)) {
            log.info("WhatsApp OTP accepted via whatsappConf defaultCode mobile={}", mobileNo);
            whatsappOtpChallengeStore.findTokenByMobile(mobileNo)
                    .ifPresent(whatsappOtpChallengeStore::delete);
            UserProfileSummary profile = userAuthRepository.findOrCreateActiveByMobileNo(mobileNo);
            TokenPair tokenPair = openSession(profile, deviceNo, LOGIN_CHANNEL_WHATSAPP);
            reportRegisterSuccessIfNeeded(profile, deviceNo, systemPlatform);
            boolean passwordSet = userAuthRepository.isPasswordSet(profile.userId());
            return new OtpVerifyResult(profile, tokenPair, passwordSet);
        }
        String otpToken = whatsappOtpChallengeStore.findTokenByMobile(mobileNo)
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));
        return verifyChallengeAndLogin(
                whatsappOtpChallengeStore,
                mobileNo,
                otpToken,
                otpCode,
                deviceNo,
                LOGIN_CHANNEL_WHATSAPP,
                systemPlatform
        );
    }

    private OtpVerifyResult verifyChallengeAndLogin(
            OtpChallengeStore challengeStore,
            String mobileNo,
            String otpToken,
            String otpCode,
            String deviceNo,
            String loginChannel,
            String systemPlatform
    ) {
        validateMobile(mobileNo);
        if (otpToken == null || otpToken.isBlank() || otpCode == null || otpCode.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        boolean allowConfiguredDefault =
                (LOGIN_CHANNEL_OTP.equals(loginChannel) && isSmsConfiguredDefaultCode(mobileNo, otpCode))
                        || (LOGIN_CHANNEL_WHATSAPP.equals(loginChannel)
                        && isWhatsAppConfiguredDefaultCode(mobileNo, otpCode));
        if (allowConfiguredDefault) {
            log.info(
                    "OTP accepted via app_config defaultCode channel={} mobile={}",
                    loginChannel,
                    mobileNo
            );
            challengeStore.findByToken(otpToken).ifPresent(challenge -> challengeStore.delete(otpToken));
        } else {
            OtpChallenge challenge = challengeStore.findByToken(otpToken)
                    .orElseThrow(() -> new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));
            if (challenge.expired(Instant.now())) {
                challengeStore.delete(otpToken);
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
            challengeStore.delete(otpToken);
        }

        UserProfileSummary profile = userAuthRepository.findOrCreateActiveByMobileNo(mobileNo);
        TokenPair tokenPair = openSession(profile, deviceNo, loginChannel);
        reportRegisterSuccessIfNeeded(profile, deviceNo, systemPlatform);
        boolean passwordSet = userAuthRepository.isPasswordSet(profile.userId());
        return new OtpVerifyResult(profile, tokenPair, passwordSet);
    }

    private void reportRegisterSuccessIfNeeded(
            UserProfileSummary profile,
            String deviceNo,
            String systemPlatform
    ) {
        if (profile == null || !profile.newlyCreated()) {
            return;
        }
        try {
            AppsFlyerS2sReporter.ReportResult result = appsFlyerS2sReporter.reportPlatformEvent(
                    AppsFlyerS2sReporter.EVENT_REGISTER_SUCCESS_PK,
                    profile.userId(),
                    profile.partnerUserId(),
                    deviceNo,
                    systemPlatform,
                    null
            );
            log.info(
                    "AF S2S REGISTER_SUCCESS_PK userId={} deviceNo={} reported={} message={}",
                    profile.userId(),
                    deviceNo,
                    result.reported(),
                    result.message()
            );
        } catch (Exception exception) {
            log.warn(
                    "AF S2S REGISTER_SUCCESS_PK failed userId={} deviceNo={} error={}",
                    profile.userId(),
                    deviceNo,
                    exception.getMessage()
            );
        }
    }

    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        RefreshTokenStore.RefreshTokenRecord record = refreshTokenStore.find(refreshToken)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        AuthSession session = sessionStore.findByUserId(record.userId())
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (session.sessionVersion() != record.sessionVersion()) {
            refreshTokenStore.delete(refreshToken);
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        UserProfileSummary profile = userAuthRepository.findByUserId(record.userId())
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        return issueAccessToken(profile, session.sessionVersion(), session.deviceId());
    }

    public TokenPair openSessionAfterMobileChange(long userId, String deviceId) {
        requireDeviceNo(deviceId);
        UserProfileSummary profile = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        return openSession(profile, deviceId, LOGIN_CHANNEL_MOBILE_CHANGE);
    }

    public void logout(AuthenticatedPrincipal principal) {
        sessionStore.delete(principal.userId());
        refreshTokenStore.deleteAllForProfile(principal.userId());
        userAuthRepository.clearSessionTokens(principal.userId());
        userAuthRepository.updateLastLogoutAt(principal.userId(), Instant.now());
    }

    public AuthenticatedPrincipal validateAccessToken(String accessToken) {
        AuthenticatedPrincipal principal = tokenIssuer.parseAccessToken(accessToken);
        AuthSession session = sessionStore.findByUserId(principal.userId()).orElse(null);
        if (session == null) {
            AuthRejectReasons.set(AuthRejectReasons.SESSION_NOT_FOUND);
            log.warn(
                    "Access token session not found userId={} mobileNo={} tokenSessionVersion={}",
                    principal.userId(),
                    principal.mobileNo(),
                    principal.sessionVersion()
            );
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (session.sessionVersion() != principal.sessionVersion()) {
            AuthRejectReasons.set(AuthRejectReasons.SESSION_VERSION_MISMATCH);
            log.warn(
                    "Access token session version mismatch userId={} mobileNo={} tokenSessionVersion={} activeSessionVersion={}",
                    principal.userId(),
                    principal.mobileNo(),
                    principal.sessionVersion(),
                    session.sessionVersion()
            );
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return principal;
    }

    private boolean passwordMatches(String rawPassword, EncryptedField encryptedPassword) {
        String storedPassword = sensitiveFieldEncryptor.decrypt(encryptedPassword);
        return MessageDigest.isEqual(
                storedPassword.getBytes(StandardCharsets.UTF_8),
                rawPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    private TokenPair openSession(UserProfileSummary profile, String deviceId, String loginChannel) {
        long nextVersion = sessionStore.findByUserId(profile.userId())
                .map(AuthSession::sessionVersion)
                .orElse(0L) + 1L;
        Instant issuedAt = Instant.now();
        AuthSession session = new AuthSession(profile.userId(), nextVersion, deviceId, loginChannel, issuedAt);
        sessionStore.save(profile.userId(), session, authProperties.refreshTokenTtl());
        refreshTokenStore.deleteAllForProfile(profile.userId());
        TokenPair tokenPair = tokenIssuer.issue(
                profile.userId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                nextVersion,
                deviceId
        );
        refreshTokenStore.save(
                tokenPair.refreshToken(),
                new RefreshTokenStore.RefreshTokenRecord(profile.userId(), nextVersion, deviceId),
                authProperties.refreshTokenTtl()
        );
        userAuthRepository.saveSessionTokens(
                profile.userId(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                Instant.now().plusSeconds(tokenPair.accessTokenExpiresInSeconds())
        );
        userAuthRepository.updateLastLoginAt(profile.userId(), Instant.now());
        return tokenPair;
    }

    private TokenPair issueAccessToken(UserProfileSummary profile, long sessionVersion, String deviceId) {
        TokenPair tokenPair = tokenIssuer.issue(
                profile.userId(),
                profile.partnerUserId(),
                profile.mobileNo(),
                sessionVersion,
                deviceId
        );
        userAuthRepository.saveAccessToken(
                profile.userId(),
                tokenPair.accessToken(),
                Instant.now().plusSeconds(tokenPair.accessTokenExpiresInSeconds())
        );
        return tokenPair;
    }

    private void requireDeviceNo(String deviceNo) {
        Objects.requireNonNull(deviceNo, "deviceNo is required");
        if (deviceNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private void enforceResendInterval(OtpChallengeStore challengeStore, String deviceNo) {
        Optional<Duration> wait = challengeStore.timeUntilResendAllowed(deviceNo);
        if (wait.isPresent()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    private void enforceDailyLimit(
            DailyCountQuery countQuery,
            String mobileNo,
            int dailyLimit,
            ZoneId dailyLimitZone
    ) {
        Instant startOfDay = ZonedDateTime.now(dailyLimitZone)
                .toLocalDate()
                .atStartOfDay(dailyLimitZone)
                .toInstant();
        long sentToday = countQuery.countSince(mobileNo, startOfDay);
        if (sentToday >= dailyLimit) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    private void validateMobile(String mobileNo) {
        if (!MobileNumberValidator.isValid(mobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
    }

    /**
     * Uses only {@code app_config.smsConf}: whitelist + defaultCode, or defaultCode when enableSms=false.
     */
    private boolean isSmsConfiguredDefaultCode(String mobileNo, String otpCode) {
        try {
            SmsConfigLoader.SmsConf conf = smsConfigLoader.loadConf();
            boolean accepted = SmsConfigLoader.acceptsConfiguredDefaultCode(conf, mobileNo, otpCode);
            if (!accepted
                    && otpCode != null
                    && conf.defaultCode() != null
                    && conf.defaultCode().trim().equals(otpCode.trim())
                    && conf.enableSms()) {
                log.info(
                        "defaultCode rejected (enableSms=true, not on userList) mobile={} userListSize={}",
                        mobileNo,
                        conf.userList() == null ? 0 : conf.userList().size()
                );
            }
            return accepted;
        } catch (Exception exception) {
            log.warn("smsConf default-code check skipped: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Uses only {@code app_config.whatsappConf}: whitelist + defaultCode, or defaultCode when
     * enableWhatsApp=false.
     */
    private boolean isWhatsAppConfiguredDefaultCode(String mobileNo, String otpCode) {
        try {
            WhatsAppConfigLoader.WhatsAppConf conf = whatsAppConfigLoader.loadConf();
            boolean accepted = WhatsAppConfigLoader.acceptsConfiguredDefaultCode(conf, mobileNo, otpCode);
            if (!accepted
                    && otpCode != null
                    && conf.defaultCode() != null
                    && conf.defaultCode().trim().equals(otpCode.trim())
                    && conf.enableWhatsApp()) {
                log.info(
                        "WhatsApp defaultCode rejected (enableWhatsApp=true, not on userList) mobile={} userListSize={}",
                        mobileNo,
                        conf.userList() == null ? 0 : conf.userList().size()
                );
            }
            return accepted;
        } catch (Exception exception) {
            log.warn("whatsappConf default-code check skipped: {}", exception.getMessage());
            return false;
        }
    }

    @FunctionalInterface
    private interface DailyCountQuery {
        long countSince(String mobileNo, Instant sinceInclusive);
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
