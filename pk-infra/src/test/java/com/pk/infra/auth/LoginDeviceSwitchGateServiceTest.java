package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.profile.FaceComparisonBaselineResolver;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginDeviceSwitchGateServiceTest {
    private UserAuthRepository userAuthRepository;
    private FaceComparisonBaselineResolver baselineResolver;
    private DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository;
    private LoginDeviceSwitchGateService gate;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        baselineResolver = mock(FaceComparisonBaselineResolver.class);
        deviceSwitchFaceRepository = mock(DeviceSwitchFaceVerificationRepository.class);
        gate = new LoginDeviceSwitchGateService(
                userAuthRepository,
                baselineResolver,
                deviceSwitchFaceRepository
        );
    }

    @Test
    void faceNotRequiredWhenNoComparableBaseline() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(false);

        assertThat(gate.evaluateFaceRequiredForMobileCheck(1L, "device-a")).isFalse();
    }

    @Test
    void faceRequiredWhenBaselineExistsAndLastDeviceUnknown() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.empty());

        assertThat(gate.evaluateFaceRequiredForMobileCheck(1L, "device-a")).isTrue();
    }

    @Test
    void faceNotRequiredWhenDeviceMatchesLastLoginDevice() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-a"));

        assertThat(gate.evaluateFaceRequiredForMobileCheck(1L, "device-a")).isFalse();
        assertThat(gate.evaluateFaceRequiredForMobileCheck(1L, "  device-a  ")).isFalse();
    }

    @Test
    void faceRequiredWhenDeviceDiffersFromLastLoginDevice() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-old"));

        assertThat(gate.evaluateFaceRequiredForMobileCheck(1L, "device-new")).isTrue();
    }

    @Test
    void assertLoginAllowedSkipsWhenFaceNotRequired() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(false);

        gate.assertLoginAllowed(1L, "device-a", null);

        verify(deviceSwitchFaceRepository, never()).findByToken(any());
    }

    @Test
    void assertLoginAllowedRejectsMissingTokenWhenFaceRequired() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("other"));

        assertThatThrownBy(() -> gate.assertLoginAllowed(1L, "device-a", null))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void assertLoginAllowedRejectsWrongUserDeviceOrExpiredTicket() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("other"));
        Instant future = Instant.now().plusSeconds(300);
        Instant past = Instant.now().minusSeconds(60);
        when(deviceSwitchFaceRepository.findByToken("tok-1"))
                .thenReturn(Optional.of(ticket(1L, "device-a", "tok-1", future)));
        when(deviceSwitchFaceRepository.findByToken("tok-wrong-user"))
                .thenReturn(Optional.of(ticket(99L, "device-a", "tok-wrong-user", future)));
        when(deviceSwitchFaceRepository.findByToken("tok-wrong-device"))
                .thenReturn(Optional.of(ticket(1L, "device-b", "tok-wrong-device", future)));
        when(deviceSwitchFaceRepository.findByToken("tok-expired"))
                .thenReturn(Optional.of(ticket(1L, "device-a", "tok-expired", past)));

        assertThatThrownBy(() -> gate.assertLoginAllowed(1L, "device-a", "tok-wrong-user"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
        assertThatThrownBy(() -> gate.assertLoginAllowed(1L, "device-a", "tok-wrong-device"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
        assertThatThrownBy(() -> gate.assertLoginAllowed(1L, "device-a", "tok-expired"))
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void assertLoginAllowedAcceptsTrimmedDeviceNoOnTicket() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("other"));
        Instant future = Instant.now().plusSeconds(300);
        when(deviceSwitchFaceRepository.findByToken("tok-1"))
                .thenReturn(Optional.of(ticket(1L, "device-a", "tok-1", future)));

        gate.assertLoginAllowed(1L, "  device-a  ", "tok-1");
    }

    @Test
    void consumeRejectsReplayWhenUpdateReturnsZeroRows() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("other"));
        when(deviceSwitchFaceRepository.consume(eq("tok-1"), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> gate.consumeFaceTokenIfRequired(1L, "device-a", "tok-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);
    }

    @Test
    void consumeSucceedsWhenFaceRequiredAndRowUpdated() {
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("other"));
        when(deviceSwitchFaceRepository.consume(eq("tok-1"), any(Instant.class))).thenReturn(true);

        gate.consumeFaceTokenIfRequired(1L, "device-a", "tok-1");

        verify(deviceSwitchFaceRepository).consume(eq("tok-1"), any(Instant.class));
    }

    @Test
    void updateLastLoginDeviceNoPersistsTrimmedDevice() {
        gate.updateLastLoginDeviceNo(1L, "  device-x  ");

        verify(userAuthRepository).updateLastLoginDeviceNo(1L, "device-x");
    }

    private static DeviceSwitchFaceVerificationRepository.FaceVerificationData ticket(
            long userId,
            String deviceNo,
            String token,
            Instant expiresAt
    ) {
        return new DeviceSwitchFaceVerificationRepository.FaceVerificationData(
                1L,
                token,
                userId,
                "U1",
                "req-1",
                deviceNo,
                "live-1",
                "IDENTITY",
                90D,
                "VERIFIED_PENDING",
                expiresAt,
                null
        );
    }
}
