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

    /** Rate limit for public {@code POST /auth/face/liveness-license}. */
    public static class DeviceSwitchLivenessLicenseRateLimit {
        private int maxInvocationsPerIp = 5;
        private int maxInvocationsPerDevice = 5;
        private int windowMinutes = 5;

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

        public Duration window() {
            return Duration.ofMinutes(Math.max(1, windowMinutes));
        }
    }
}
