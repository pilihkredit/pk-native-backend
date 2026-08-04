package com.pk.app.profile.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import com.pk.app.profile.dto.request.MobileChangeOtpSendRequest;
import com.pk.app.profile.dto.request.MobileChangeOtpVerifyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class ProfileMobileChangeEndpointTest {
    @Test
    void mobileChangeVerificationRequiresLatestDevice() {
        assertThat(Arrays.stream(MobileChangeOtpVerifyRequest.class.getRecordComponents())
                .map(component -> component.getName()))
                .contains("device");
    }

    @Test
    void mobileChangeOtpSendRequestDoesNotSelectDeliveryChannel() {
        assertThat(Arrays.stream(MobileChangeOtpSendRequest.class.getRecordComponents())
                .map(component -> component.getName()))
                .doesNotContain("channel");
    }

    @Test
    void exposesOnlyVerifiedMobileChangeEndpoints() {
        Set<String> paths = Arrays.stream(ProfileController.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .collect(Collectors.toSet());

        assertThat(paths).contains(
                "/mobile/face/verify",
                "/mobile/otp/send",
                "/mobile/otp/whatsapp/send",
                "/mobile/otp/verify"
        );
        assertThat(paths).doesNotContain("/mobile/change");
    }
}
