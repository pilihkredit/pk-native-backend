package com.pk.app.review.dto.response;

public record ReviewGuideClaimResponse(
        boolean shouldShow,
        Long guideId
) {
}
