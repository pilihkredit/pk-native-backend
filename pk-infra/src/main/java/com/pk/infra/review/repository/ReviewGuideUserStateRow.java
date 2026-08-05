package com.pk.infra.review.repository;

import java.time.Instant;

public record ReviewGuideUserStateRow(
        long userId,
        Instant storeJumpAt
) {
}
