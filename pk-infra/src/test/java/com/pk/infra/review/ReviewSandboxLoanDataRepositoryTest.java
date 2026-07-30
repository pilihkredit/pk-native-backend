package com.pk.infra.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.review.ReviewSandboxLoanDataPort.ReviewLoanSnapshot;
import com.pk.infra.review.mapper.ReviewSandboxLoanDataMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewSandboxLoanDataRepositoryTest {
    @Test
    void exposesLoanSnapshotsReturnedByMapper() {
        ReviewSandboxLoanDataMapper mapper = mock(ReviewSandboxLoanDataMapper.class);
        ReviewLoanSnapshot snapshot = new ReviewLoanSnapshot(
                "LOAN-1", "REVIEW-LOAN-1", "USER-1", "REVIEW-USER-1", "BILL-1",
                new BigDecimal("1000000"), new BigDecimal("970000"), Instant.parse("2026-07-30T00:00:00Z")
        );
        when(mapper.findByLoanApplyId("LOAN-1")).thenReturn(snapshot);
        when(mapper.findDisbursedByPartnerUserId("USER-1")).thenReturn(List.of(snapshot));
        var repository = new ReviewSandboxLoanDataRepository(mapper);

        assertThat(repository.findByLoanApplyId("LOAN-1")).contains(snapshot);
        assertThat(repository.findDisbursedByPartnerUserId("USER-1")).containsExactly(snapshot);
    }
}
