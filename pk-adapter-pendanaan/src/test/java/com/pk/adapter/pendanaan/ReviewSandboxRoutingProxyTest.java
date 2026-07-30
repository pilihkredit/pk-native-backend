package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.ProfileSyncModule;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ReviewSandboxRoutingProxyTest {
    private final LenderCreditPort live = mock(LenderCreditPort.class);
    private final LenderCreditPort review = mock(LenderCreditPort.class);
    private final ReviewSandboxConfigPort config = mock(ReviewSandboxConfigPort.class);
    private final LenderCreditPort routed = ReviewSandboxRoutingProxy.wrap(
            LenderCreditPort.class,
            live,
            review,
            config
    );

    @AfterEach
    void clearContext() {
        LenderInteractionContext.clear();
    }

    @Test
    void routesReviewMobileToSandboxWithoutCallingLiveLender() {
        var expected = new LenderCreditPort.LenderCreditStatusResult(
                "SUCCESS", "REVIEW-USER", "REVIEW-CREDIT", null, null,
                new BigDecimal("500000"), new BigDecimal("3000000"),
                new BigDecimal("3000000"), new BigDecimal("3000000"),
                new BigDecimal("100000"), null
        );
        when(config.findEnabledScenario("628111000000")).thenReturn(Optional.of(reviewScenario()));
        when(review.queryStatus("APPLY-1")).thenReturn(expected);
        LenderInteractionContext.setMobileNo("628111000000");

        assertThat(routed.queryStatus("APPLY-1")).isSameAs(expected);
        verify(live, never()).queryStatus("APPLY-1");
    }

    @Test
    void routesOrdinaryMobileToLiveLender() {
        var expected = mock(LenderCreditPort.LenderCreditStatusResult.class);
        when(config.findEnabledScenario("628122000000")).thenReturn(Optional.empty());
        when(live.queryStatus("APPLY-2")).thenReturn(expected);
        LenderInteractionContext.setMobileNo("628122000000");

        assertThat(routed.queryStatus("APPLY-2")).isSameAs(expected);
        verify(review, never()).queryStatus("APPLY-2");
    }

    @Test
    void routesBackgroundProfileSyncUsingCommandMobile() {
        LenderProfileSyncPort liveProfile = mock(LenderProfileSyncPort.class);
        LenderProfileSyncPort reviewProfile = mock(LenderProfileSyncPort.class);
        LenderProfileSyncPort routedProfile = ReviewSandboxRoutingProxy.wrap(
                LenderProfileSyncPort.class,
                liveProfile,
                reviewProfile,
                config
        );
        var command = new LenderProfileSyncPort.LenderProfileSyncCommand(
                "REQUEST-1", "USER-1", "628111000000", ProfileSyncModule.PERSONAL, null, null
        );
        var expected = new LenderProfileSyncPort.LenderProfileSyncResult("REVIEW-USER", "{}", null);
        when(config.findEnabledScenario("628111000000")).thenReturn(Optional.of(reviewScenario()));
        when(reviewProfile.syncModule(command)).thenReturn(expected);

        assertThat(routedProfile.syncModule(command)).isSameAs(expected);
        verify(liveProfile, never()).syncModule(command);
    }

    private static ReviewSandboxConfigPort.ReviewSandboxScenario reviewScenario() {
        return new ReviewSandboxConfigPort.ReviewSandboxScenario(
                "DEFAULT", new BigDecimal("500000"), new BigDecimal("3000000"),
                new BigDecimal("100000"), new BigDecimal("0.18"), new BigDecimal("0.97"), 6, 30,
                "REVIEW_BANK", "Review Bank", "0000000000000000"
        );
    }
}
