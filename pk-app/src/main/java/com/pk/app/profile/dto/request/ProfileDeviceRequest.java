package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Device payload submitted with each onboarding write API.
 */
public record ProfileDeviceRequest(
        @NotBlank @Size(max = 64) String appName,
        @NotBlank @Size(max = 32) String appVersion,
        @NotBlank @Size(max = 128) String packageName,
        @NotBlank @Size(max = 128) String deviceNo,
        @NotBlank
        @Pattern(regexp = "ios|android", flags = Pattern.Flag.CASE_INSENSITIVE)
        @Size(max = 16)
        String systemPlatform,
        @Size(max = 128) String adId,
        Map<String, Object> deviceOtherInfo
) {
}
