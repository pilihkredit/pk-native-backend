package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.MobileChangeOtpChallenge;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.MobileChangeOtpChallengeStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.infra.auth.AuthOtpConfigLoader;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.auth.OtpCodeGenerator;
import com.pk.infra.auth.SmsConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class MobileChangeOtpFacade {
    private static final String PURPOSE = "MOBILE_CHANGE";

    private final UserAuthRepository userAuthRepository;
    private final MobileChangeFaceVerificationRepository faceRepository;
    private final MobileChangeOtpChallengeStore challengeStore;
    private final SmsSendLogRepository smsSendLogRepository;
    private final SmsSender smsSender;
    private final AuthOtpConfigLoader otpConfigLoader;
    private final SmsConfigLoader smsConfigLoader;

    public MobileChangeOtpFacade(
            UserAuthRepository userAuthRepository,
            MobileChangeFaceVerificationRepository faceRepository,
            MobileChangeOtpChallengeStore challengeStore,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender,
            AuthOtpConfigLoader otpConfigLoader,
            SmsConfigLoader smsConfigLoader
    ) {
        this.userAuthRepository = userAuthRepository;
        this.faceRepository = faceRepository;
        this.challengeStore = challengeStore;
        this.smsSendLogRepository = smsSendLogRepository;
        this.smsSender = smsSender;
        this.otpConfigLoader = otpConfigLoader;
        this.smsConfigLoader = smsConfigLoader;
    }

    public OtpSendResult send(long userId, OtpSendCommand command) {
        String newMobileNo = normalize(command.newMobileNo());
        if (!MobileNumberValidator.isValid(newMobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        UserProfileSummary current = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (newMobileNo.equals(current.mobileNo())) {
            throw new ApiException(ApiCode.MOBILE_NUMBER_UNCHANGED);
        }
        if (userAuthRepository.findActiveByMobileNoExcludingUserId(newMobileNo, userId).isPresent()) {
            throw new ApiException(ApiCode.MOBILE_ALREADY_REGISTERED);
        }
        var face = faceRepository.findVerifiedByToken(command.faceVerifyToken())
                .filter(value -> value.userId() == userId)
                .filter(value -> value.deviceNo().equals(command.deviceNo()))
                .filter(value -> value.usableAt(Instant.now()))
                .orElseThrow(() -> new ApiException(ApiCode.FACE_RECOGNITION_FAILED));
        if (!face.faceVerifyToken().equals(command.faceVerifyToken())) {
            throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
        }

        AuthOtpConfigLoader.AuthOtpConfig otpConfig = otpConfigLoader.load();
        if (challengeStore.timeUntilResendAllowed(command.deviceNo()).isPresent()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
        Instant startOfDay = ZonedDateTime.now(otpConfig.otpDailyLimitZone())
                .toLocalDate().atStartOfDay(otpConfig.otpDailyLimitZone()).toInstant();
        if (smsSendLogRepository.countSince(newMobileNo, startOfDay) >= otpConfig.otpDailyLimit()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }

        SmsConfigLoader.SmsConf smsConf = smsConfigLoader.loadConf();
        String otpToken = OtpCodeGenerator.token();
        String otpCode = OtpCodeGenerator.numericCode(smsConf.codeLength());
        Duration ttl = Duration.ofSeconds(smsConf.expireTimeSeconds());
        MobileChangeOtpChallenge challenge = new MobileChangeOtpChallenge(
                otpToken,
                userId,
                newMobileNo,
                command.deviceNo(),
                command.faceVerifyToken(),
                otpCode,
                Instant.now().plus(ttl)
        );
        challengeStore.save(challenge, ttl);
        long logId = smsSendLogRepository.insert(new SmsSendLogRepository.SmsSendLogEntry(
                Optional.of(userId), newMobileNo, command.deviceNo(), otpToken, otpCode, PURPOSE));
        SmsSendResult sendResult = smsSender.send(newMobileNo, otpCode);
        smsSendLogRepository.updateProviderResult(logId, sendResult);
        if (!sendResult.success()) {
            challengeStore.delete(otpToken);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        challengeStore.markSent(command.deviceNo(), otpConfig.otpResendInterval());
        faceRepository.attachOtpToken(command.faceVerifyToken(), otpToken);
        return new OtpSendResult(
                command.requestId(), otpToken, ttl.toSeconds(), otpConfig.otpResendInterval().toSeconds());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record OtpSendCommand(String requestId, String newMobileNo, String faceVerifyToken, String deviceNo) {
    }

    public record OtpSendResult(String requestId, String otpToken, long expiresIn, long resendAfter) {
    }
}
