package com.pk.infra.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReviewGuideMinJumpRatingLoaderTest {
    @Test
    void loadsConfiguredThreshold() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(ReviewGuideMinJumpRatingLoader.CONFIG_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(1L, ReviewGuideMinJumpRatingLoader.CONFIG_KEY, "4")
        ));

        int rating = new ReviewGuideMinJumpRatingLoader(repository, new ObjectMapper()).load();

        assertThat(rating).isEqualTo(4);
    }

    @Test
    void fallsBackToDefaultWhenMissing() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(ReviewGuideMinJumpRatingLoader.CONFIG_KEY)).thenReturn(Optional.empty());

        int rating = new ReviewGuideMinJumpRatingLoader(repository, new ObjectMapper()).load();

        assertThat(rating).isEqualTo(ReviewGuideMinJumpRatingLoader.DEFAULT_MIN_JUMP_RATING);
    }
}
