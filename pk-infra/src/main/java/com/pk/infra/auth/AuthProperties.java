package com.pk.infra.auth;

import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.auth")
public class AuthProperties {
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private Duration otpTtl = Duration.ofMinutes(5);
    private Duration otpResendInterval = Duration.ofSeconds(60);
    private int otpDailyLimit = 10;
    private ZoneId otpDailyLimitZone = ZoneId.of("Asia/Jakarta");
    private String jwtSecret = "local-dev-secret-change-in-prod-min-32-chars";
    private int passwordMaxFailedAttempts = 5;
    private Duration passwordLockDuration = Duration.ofMinutes(15);
    private boolean otpBypassEnabled = false;
    private String otpBypassCode = "123456";

    public Duration accessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration refreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration otpTtl() {
        return otpTtl;
    }

    public void setOtpTtl(Duration otpTtl) {
        this.otpTtl = otpTtl;
    }

    public Duration otpResendInterval() {
        return otpResendInterval;
    }

    public void setOtpResendInterval(Duration otpResendInterval) {
        this.otpResendInterval = otpResendInterval;
    }

    public int otpDailyLimit() {
        return otpDailyLimit;
    }

    public void setOtpDailyLimit(int otpDailyLimit) {
        this.otpDailyLimit = otpDailyLimit;
    }

    public ZoneId otpDailyLimitZone() {
        return otpDailyLimitZone;
    }

    public void setOtpDailyLimitZone(ZoneId otpDailyLimitZone) {
        this.otpDailyLimitZone = otpDailyLimitZone;
    }

    public String jwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public int passwordMaxFailedAttempts() {
        return passwordMaxFailedAttempts;
    }

    public void setPasswordMaxFailedAttempts(int passwordMaxFailedAttempts) {
        this.passwordMaxFailedAttempts = passwordMaxFailedAttempts;
    }

    public Duration passwordLockDuration() {
        return passwordLockDuration;
    }

    public void setPasswordLockDuration(Duration passwordLockDuration) {
        this.passwordLockDuration = passwordLockDuration;
    }

    public boolean otpBypassEnabled() {
        return otpBypassEnabled;
    }

    public void setOtpBypassEnabled(boolean otpBypassEnabled) {
        this.otpBypassEnabled = otpBypassEnabled;
    }

    public String otpBypassCode() {
        return otpBypassCode;
    }

    public void setOtpBypassCode(String otpBypassCode) {
        this.otpBypassCode = otpBypassCode;
    }
}
