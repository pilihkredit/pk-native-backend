package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Device payload submitted with each onboarding write API.
 * Field set is identical to the lender OpenAPI {@code device} object
 * (https://ek8l1y505u.feishu.cn/wiki/ExC1wwGVWiQ8CqkrRdEcc2ZinKb).
 * {@code deviceOtherInfo} accepts any sub-fields defined in lender {@code deviceOtherInfo}.
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
        @Size(max = 64) String phoneBrand,
        @Size(max = 64) String phoneBrandModel,
        @Size(max = 64) String mac,
        @Size(max = 32) String systemVersion,
        @Size(max = 64) String deliveryPlatform,
        Integer cpuCores,
        Long memoryTotal,
        Long sdCardTotal,
        @Size(max = 128) String adId,
        @Size(max = 64) String idfv,
        @Size(max = 64) String idfa,
        @Size(max = 1024) String extParam,
        Map<String, Object> deviceOtherInfo
) {
}
