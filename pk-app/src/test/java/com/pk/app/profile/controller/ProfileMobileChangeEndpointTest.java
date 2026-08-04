package com.pk.app.profile.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class ProfileMobileChangeEndpointTest {
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
                "/mobile/otp/verify"
        );
        assertThat(paths).doesNotContain("/mobile/change");
    }
}
