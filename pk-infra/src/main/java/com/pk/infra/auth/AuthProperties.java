package com.pk.infra.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.auth")
public class AuthProperties {
    private Duration accessTokenTtl = Duration.ofMinutes(15);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private Duration otpTtl = Duration.ofMinutes(5);
    private String jwtSecret = "local-dev-secret-change-in-prod-min-32-chars";
    private DeviceSwitchLivenessLicenseRateLimit deviceSwitchLivenessLicenseRateLimit =
            new DeviceSwitchLivenessLicenseRateLimit();
    private FaceVerifyTicket faceVerifyTicket = new FaceVerifyTicket();

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

    public String jwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public DeviceSwitchLivenessLicenseRateLimit deviceSwitchLivenessLicenseRateLimit() {
        return deviceSwitchLivenessLicenseRateLimit;
    }

    public void setDeviceSwitchLivenessLicenseRateLimit(
            DeviceSwitchLivenessLicenseRateLimit deviceSwitchLivenessLicenseRateLimit
    ) {
        this.deviceSwitchLivenessLicenseRateLimit = deviceSwitchLivenessLicenseRateLimit == null
                ? new DeviceSwitchLivenessLicenseRateLimit()
                : deviceSwitchLivenessLicenseRateLimit;
    }

    public FaceVerifyTicket faceVerifyTicket() {
        return faceVerifyTicket;
    }

    public void setFaceVerifyTicket(FaceVerifyTicket faceVerifyTicket) {
        this.faceVerifyTicket = faceVerifyTicket == null ? new FaceVerifyTicket() : faceVerifyTicket;
    }

    /** TTL for face-verify tickets (device-switch login, bank-card add, mobile change). */
    public static class FaceVerifyTicket {
        private Duration ttl = Duration.ofMinutes(5);
        private Duration deviceSwitchLoginTtl;
        private Duration bankCardAddTtl;
        private Duration mobileChangeTtl;

        public Duration ttl() {
            return normalizeDefault(ttl);
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public Duration deviceSwitchLoginTtl() {
            return effective(deviceSwitchLoginTtl);
        }

        public void setDeviceSwitchLoginTtl(Duration deviceSwitchLoginTtl) {
            this.deviceSwitchLoginTtl = deviceSwitchLoginTtl;
        }

        public Duration bankCardAddTtl() {
            return effective(bankCardAddTtl);
        }

        public void setBankCardAddTtl(Duration bankCardAddTtl) {
            this.bankCardAddTtl = bankCardAddTtl;
        }

        public Duration mobileChangeTtl() {
            return effective(mobileChangeTtl);
        }

        public void setMobileChangeTtl(Duration mobileChangeTtl) {
            this.mobileChangeTtl = mobileChangeTtl;
        }

        private Duration effective(Duration override) {
            return normalizeDefault(override != null ? override : ttl);
        }

        private static Duration normalizeDefault(Duration value) {
            if (value == null || value.isZero() || value.isNegative()) {
                return Duration.ofMinutes(5);
            }
            Duration minimum = Duration.ofMinutes(1);
            return value.compareTo(minimum) < 0 ? minimum : value;
        }
    }

    /** Rate limit for public {@code POST /auth/face/liveness-license}. */
    public static class DeviceSwitchLivenessLicenseRateLimit {
        private int maxInvocationsPerIp = 100;
        private int maxInvocationsPerDevice = 5;
        private int windowMinutes = 10;
        private int blockMinutes = 15;

        public int maxInvocationsPerIp() {
            return maxInvocationsPerIp;
        }

        public void setMaxInvocationsPerIp(int maxInvocationsPerIp) {
            this.maxInvocationsPerIp = maxInvocationsPerIp;
        }

        public int maxInvocationsPerDevice() {
            return maxInvocationsPerDevice;
        }

        public void setMaxInvocationsPerDevice(int maxInvocationsPerDevice) {
            this.maxInvocationsPerDevice = maxInvocationsPerDevice;
        }

        public int windowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }

        public int blockMinutes() {
            return blockMinutes;
        }

        public void setBlockMinutes(int blockMinutes) {
            this.blockMinutes = blockMinutes;
        }

        public Duration window() {
            return Duration.ofMinutes(Math.max(1, windowMinutes));
        }

        public Duration block() {
            return Duration.ofMinutes(Math.max(1, blockMinutes));
        }
    }
}
