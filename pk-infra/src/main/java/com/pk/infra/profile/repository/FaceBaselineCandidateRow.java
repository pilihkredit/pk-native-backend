package com.pk.infra.profile.repository;

import java.time.Instant;

public record FaceBaselineCandidateRow(
        String candidateFaceEncryptedRef,
        String sourceType,
        Instant comparedAt
) {
}
