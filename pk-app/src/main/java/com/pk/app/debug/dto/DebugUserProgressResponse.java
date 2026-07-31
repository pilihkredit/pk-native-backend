package com.pk.app.debug.dto;

import java.time.Instant;
import java.util.List;

public record DebugUserProgressResponse(
        boolean found,
        UserInfo user,
        ProgressInfo progress,
        List<InteractionInfo> interactions
) {
    public static DebugUserProgressResponse notFound() {
        return new DebugUserProgressResponse(false, null, null, List.of());
    }

    public record UserInfo(
            long userId,
            String partnerUserId,
            String mobileNo
    ) {
    }

    public record ProgressInfo(
            String kycStatus,
            List<String> completedModules,
            List<String> missingModules
    ) {
    }

    public record InteractionInfo(
            long id,
            String providerCode,
            String interactionNo,
            String businessType,
            String businessId,
            String httpMethod,
            String endpoint,
            String responseCode,
            String responseMsg,
            boolean success,
            Integer durationMs,
            String requestRef,
            String responseRef,
            Instant createdAt
    ) {
    }
}
