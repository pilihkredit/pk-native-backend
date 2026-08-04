package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.MobileChangeOtpChallenge;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.MobileChangeOtpChallengeStore;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository.UserMobileChangeLogEntry;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.infra.auth.AuthServiceFacade;
import com.pk.infra.auth.MobileNumberValidator;
import com.pk.infra.auth.SmsConfigLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileChangeFacade {
    private final UserAuthRepository userAuthRepository;
    private final UserMobileChangeLogRepository changeLogRepository;
    private final MobileChangeFaceVerificationRepository faceRepository;
    private final MobileChangeOtpChallengeStore challengeStore;
    private final AuthServiceFacade authServiceFacade;
    private final SmsConfigLoader smsConfigLoader;

    public MobileChangeFacade(
            UserAuthRepository userAuthRepository,
            UserMobileChangeLogRepository changeLogRepository,
            MobileChangeFaceVerificationRepository faceRepository,
            MobileChangeOtpChallengeStore challengeStore,
            AuthServiceFacade authServiceFacade,
            SmsConfigLoader smsConfigLoader
    ) {
        this.userAuthRepository = userAuthRepository;
        this.changeLogRepository = changeLogRepository;
        this.faceRepository = faceRepository;
        this.challengeStore = challengeStore;
        this.authServiceFacade = authServiceFacade;
        this.smsConfigLoader = smsConfigLoader;
    }

    @Transactional
    public MobileChangeResult verifyAndChange(long userId, MobileChangeVerifyCommand command) {
        String newMobileNo = normalize(command.newMobileNo());
        if (!MobileNumberValidator.isValid(newMobileNo)) {
            throw new ApiException(ApiCode.INVALID_MOBILE_NUMBER);
        }
        MobileChangeOtpChallenge challenge = challengeStore.findByToken(command.otpToken())
                .orElseThrow(() -> new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));
        if (challenge.expired(Instant.now())) {
            challengeStore.delete(command.otpToken());
            throw new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
        }
        if (challenge.userId() != userId
                || !challenge.newMobileNo().equals(newMobileNo)
                || !challenge.deviceNo().equals(command.deviceNo())
                || !challenge.faceVerifyToken().equals(command.faceVerifyToken())) {
            throw new ApiException(ApiCode.OTP_TOKEN_MISMATCH);
        }
        boolean configuredDefault = SmsConfigLoader.acceptsConfiguredDefaultCode(
                smsConfigLoader.loadConf(), newMobileNo, command.otpCode());
        if (!configuredDefault && !secureEquals(challenge.otpCode(), command.otpCode())) {
            throw new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
        }

        var face = faceRepository.findVerifiedByToken(command.faceVerifyToken())
                .filter(value -> value.userId() == userId)
                .filter(value -> value.deviceNo().equals(command.deviceNo()))
                .filter(value -> command.otpToken().equals(value.otpToken()))
                .filter(value -> value.usableAt(Instant.now()))
                .orElseThrow(() -> new ApiException(ApiCode.FACE_RECOGNITION_FAILED));
        UserProfileSummary current = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ApiCode.UNAUTHORIZED_REQUEST));
        if (newMobileNo.equals(current.mobileNo())) {
            throw new ApiException(ApiCode.MOBILE_NUMBER_UNCHANGED);
        }
        if (userAuthRepository.findActiveByMobileNoExcludingUserId(newMobileNo, userId).isPresent()) {
            throw new ApiException(ApiCode.MOBILE_ALREADY_REGISTERED);
        }

        userAuthRepository.updateMobileNo(userId, newMobileNo);
        changeLogRepository.insert(new UserMobileChangeLogEntry(
                userId,
                current.partnerUserId(),
                current.mobileNo(),
                newMobileNo,
                "SUCCESS",
                "USER",
                face.faceVerifyToken(),
                challenge.otpToken()
        ));
        if (!faceRepository.promote(face.faceVerifyToken(), Instant.now())) {
            throw new ApiException(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
        }
        TokenPair tokenPair = authServiceFacade.openSessionAfterMobileChange(userId, command.deviceNo());
        challengeStore.delete(command.otpToken());
        return new MobileChangeResult(command.requestId(), true, newMobileNo, tokenPair);
    }

    private static boolean secureEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.trim().getBytes(StandardCharsets.UTF_8));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record MobileChangeVerifyCommand(
            String requestId,
            String newMobileNo,
            String faceVerifyToken,
            String otpToken,
            String otpCode,
            String deviceNo
    ) {
    }

    public record MobileChangeResult(String requestId, boolean changed, String mobileNo, TokenPair tokenPair) {
    }
}
