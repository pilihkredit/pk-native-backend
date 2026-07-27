package com.pk.core.profile.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TrustDecisionSessionStateTest {
    @Test
    void keepsTechnicalCompletionSeparateFromBusinessResult() {
        TrustDecisionSessionState state = new TrustDecisionSessionState(
                true,
                true,
                "fail",
                0.41D,
                "live-seq",
                "{}",
                new OcrSessionState.OcrParsedFields(
                        "TEST USER",
                        "3201010101010001",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                "encrypted://id-card",
                10L,
                Instant.parse("2026-07-27T00:00:00Z")
        );

        assertThat(state.ocrCompleted()).isTrue();
        assertThat(state.livenessCompleted()).isTrue();
        assertThat(state.livenessResult()).isEqualTo("fail");
        assertThat(state.livenessScore()).isEqualTo(0.41D);
    }
}
