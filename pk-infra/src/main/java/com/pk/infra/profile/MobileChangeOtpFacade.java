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
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.core.auth.port.WhatsAppSender;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.infra.auth.AuthOtpConfigLoader;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.auth.OtpCodeGenerator;
import com.pk.infra.auth.SmsConfigLoader;
import com.pk.infra.auth.WhatsAppConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class MobileChangeOtpFacade {
    private static final String PURPOSE = "MOBILE_CHANGE";
    private static final String CHANNEL_SMS = "SMS";
    private static final String CHANNEL_WHATSAPP = "WHATSAPP";

    private final UserAuthRepository userAuthRepository;
    private final MobileChangeFaceVerificationRepository faceRepository;
    private final MobileChangeOtpChallengeStore challengeStore;
    private final SmsSendLogRepository smsSendLogRepository;
    private final SmsSender smsSender;
    private final WhatsAppSendLogRepository whatsAppSendLogRepository;
    private final WhatsAppSender whatsAppSender;
    private final AuthOtpConfigLoader otpConfigLoader;
    private final SmsConfigLoader smsConfigLoader;
    private final WhatsAppConfigLoader whatsAppConfigLoader;

    public MobileChangeOtpFacade(
            UserAuthRepository userAuthRepository,
            MobileChangeFaceVerificationRepository faceRepository,
            MobileChangeOtpChallengeStore challengeStore,
            SmsSendLogRepository smsSendLogRepository,
            SmsSender smsSender,
            WhatsAppSendLogRepository whatsAppSendLogRepository,
            WhatsAppSender whatsAppSender,
            AuthOtpConfigLoader otpConfigLoader,
            SmsConfigLoader smsConfigLoader,
            WhatsAppConfigLoader whatsAppConfigLoader
    ) {
        this.userAuthRepository = userAuthRepository;
        this.faceRepository = faceRepository;
        this.challengeStore = challengeStore;
        this.smsSendLogRepository = smsSendLogRepository;
        this.smsSender = smsSender;
        this.whatsAppSendLogRepository = whatsAppSendLogRepository;
        this.whatsAppSender = whatsAppSender;
        this.otpConfigLoader = otpConfigLoader;
        this.smsConfigLoader = smsConfigLoader;
        this.whatsAppConfigLoader = whatsAppConfigLoader;
    }

    public OtpSendResult send(long userId, OtpSendCommand command) {
        String newMobileNo = normalize(command.newMobileNo());
        String channel = normalizeChannel(command.channel());
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
        String otpToken = OtpCodeGenerator.token();
        String otpCode;
        Duration ttl;
        Duration resendInterval;
        SmsSendResult sendResult;

        if (CHANNEL_WHATSAPP.equals(channel)) {
            WhatsAppConfigLoader.WhatsAppConf whatsAppConf = whatsAppConfigLoader.loadConf();
            if (whatsAppSendLogRepository.countSince(newMobileNo, startOfDay)
                    >= whatsAppConfigLoader.loadDailyLimit()) {
                throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
            }
            otpCode = OtpCodeGenerator.sixDigits();
            ttl = whatsAppConf.expireTime();
            resendInterval = whatsAppConf.minInterval();
        } else {
            if (smsSendLogRepository.countSince(newMobileNo, startOfDay) >= otpConfig.otpDailyLimit()) {
                throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
            }
            SmsConfigLoader.SmsConf smsConf = smsConfigLoader.loadConf();
            otpCode = OtpCodeGenerator.numericCode(smsConf.codeLength());
            ttl = Duration.ofSeconds(smsConf.expireTimeSeconds());
            resendInterval = otpConfig.otpResendInterval();
        }

        MobileChangeOtpChallenge challenge = new MobileChangeOtpChallenge(
                otpToken,
                userId,
                newMobileNo,
                command.deviceNo(),
                command.faceVerifyToken(),
                otpCode,
                Instant.now().plus(ttl),
                channel
        );
        challengeStore.save(challenge, ttl);
        if (CHANNEL_WHATSAPP.equals(channel)) {
            long logId = whatsAppSendLogRepository.insert(new WhatsAppSendLogRepository.WhatsAppSendLogEntry(
                    Optional.of(userId), newMobileNo, command.deviceNo(), otpToken, otpCode, PURPOSE));
            sendResult = whatsAppSender.send(newMobileNo, otpCode);
            whatsAppSendLogRepository.updateProviderResult(logId, sendResult);
        } else {
            long logId = smsSendLogRepository.insert(new SmsSendLogRepository.SmsSendLogEntry(
                    Optional.of(userId), newMobileNo, command.deviceNo(), otpToken, otpCode, PURPOSE));
            sendResult = smsSender.send(newMobileNo, otpCode);
            smsSendLogRepository.updateProviderResult(logId, sendResult);
        }
        if (!sendResult.success()) {
            challengeStore.delete(otpToken);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        challengeStore.markSent(command.deviceNo(), resendInterval);
        faceRepository.attachOtpToken(command.faceVerifyToken(), otpToken);
        return new OtpSendResult(
                command.requestId(), otpToken, ttl.toSeconds(), resendInterval.toSeconds());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeChannel(String value) {
        if (value == null || value.isBlank()) {
            return CHANNEL_SMS;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!CHANNEL_SMS.equals(normalized) && !CHANNEL_WHATSAPP.equals(normalized)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return normalized;
    }

    public record OtpSendCommand(
            String requestId,
            String newMobileNo,
            String faceVerifyToken,
            String deviceNo,
            String channel
    ) {
    }

    public record OtpSendResult(String requestId, String otpToken, long expiresIn, long resendAfter) {
    }
}
