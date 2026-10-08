package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.profile.TrustDecisionIdentityFacade;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeviceSwitchLivenessLicenseFacadeTest {
    private UserAuthRepository userAuthRepository;
    private LoginDeviceSwitchGateService loginDeviceSwitchGateService;
    private TrustDecisionIdentityFacade trustDecisionIdentityFacade;
    private DeviceSwitchLivenessLicenseRateLimiter rateLimiter;
    private DeviceSwitchSecurityConfigLoader configLoader;
    private DeviceSwitchLivenessLicenseFacade facade;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        loginDeviceSwitchGateService = mock(LoginDeviceSwitchGateService.class);
        trustDecisionIdentityFacade = mock(TrustDecisionIdentityFacade.class);
        rateLimiter = mock(DeviceSwitchLivenessLicenseRateLimiter.class);
        configLoader = mock(DeviceSwitchSecurityConfigLoader.class);
        when(configLoader.loadSettings())
                .thenReturn(DeviceSwitchSecurityConfigLoader.Settings.defaults());
        facade = new DeviceSwitchLivenessLicenseFacade(
                userAuthRepository,
                loginDeviceSwitchGateService,
                trustDecisionIdentityFacade,
                rateLimiter,
                configLoader
        );
    }

    @Test
    void obtainsLicenseWhenFaceRequired() {
        when(userAuthRepository.findByMobileNo("81234567890"))
                .thenReturn(Optional.of(new UserProfileSummary(10L, "U10001", "81234567890", false)));
        when(loginDeviceSwitchGateService.evaluateFaceRequiredForMobileCheck(10L, "device-1"))
                .thenReturn(true);
        when(trustDecisionIdentityFacade.obtainLivenessLicense(
                eq(10L),
                eq("U10001"),
                eq("81234567890"),
                eq(600),
                anyString(),
                anyString()
        )).thenReturn(new TrustDecisionIdentityFacade.LivenessLicenseResult("lic", 1L, "seq"));

        var result = facade.obtainLicense(
                "81234567890",
                "device-1",
                600,
                "1.2.3.4",
                "req",
                "trace"
        );

        assertThat(result.license()).isEqualTo("lic");
        verify(rateLimiter).checkAndRecord("1.2.3.4", "device-1");
    }

    @Test
    void rejectsWhenFaceNotRequired() {
        when(userAuthRepository.findByMobileNo("81234567890"))
                .thenReturn(Optional.of(new UserProfileSummary(10L, "U10001", "81234567890", false)));
        when(loginDeviceSwitchGateService.evaluateFaceRequiredForMobileCheck(10L, "device-1"))
                .thenReturn(false);

        assertThatThrownBy(() -> facade.obtainLicense(
                "81234567890",
                "device-1",
                600,
                "1.2.3.4",
                "req",
                "trace"
        ))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);

        verify(rateLimiter, never()).checkAndRecord(anyString(), anyString());
        verify(trustDecisionIdentityFacade, never()).obtainLivenessLicense(
                anyLong(),
                anyString(),
                anyString(),
                anyInt(),
                anyString(),
                anyString()
        );
    }

    @Test
    void nullDurationFallsBackToConfiguredDefault() {
        when(userAuthRepository.findByMobileNo("81234567890"))
                .thenReturn(Optional.of(new UserProfileSummary(10L, "U10001", "81234567890", false)));
        when(loginDeviceSwitchGateService.evaluateFaceRequiredForMobileCheck(10L, "device-1"))
                .thenReturn(true);
        when(trustDecisionIdentityFacade.obtainLivenessLicense(
                eq(10L), eq("U10001"), eq("81234567890"), eq(600), anyString(), anyString()
        )).thenReturn(new TrustDecisionIdentityFacade.LivenessLicenseResult("lic", 1L, "seq"));

        facade.obtainLicense("81234567890", "device-1", null, "1.2.3.4", "req", "trace");

        verify(trustDecisionIdentityFacade).obtainLivenessLicense(
                eq(10L), eq("U10001"), eq("81234567890"), eq(600), anyString(), anyString());
    }

    @Test
    void rejectsDeviceNoBeyondConfiguredMaxLength() {
        when(configLoader.loadSettings()).thenReturn(new DeviceSwitchSecurityConfigLoader.Settings(
                100, 5, java.time.Duration.ofMinutes(10), java.time.Duration.ofMinutes(15),
                5, java.time.Duration.ofHours(1), 600, 8
        ));

        assertThatThrownBy(() -> facade.obtainLicense(
                "81234567890",
                "device-too-long",
                600,
                "1.2.3.4",
                "req",
                "trace"
        ))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);

        verify(rateLimiter, never()).checkAndRecord(anyString(), anyString());
    }
}
