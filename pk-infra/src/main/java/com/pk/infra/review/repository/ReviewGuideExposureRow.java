package com.pk.infra.review.repository;

import java.time.Instant;

public record ReviewGuideExposureRow(
        long id,
        String scene,
        Instant clickedAt
) {
}
