package com.pk.infra.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.review.port.ReviewGuideRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewGuideInfraConfiguration {
    @Bean
    ReviewGuideMinJumpRatingLoader reviewGuideMinJumpRatingLoader(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        return new ReviewGuideMinJumpRatingLoader(appConfigRepository, objectMapper);
    }

    @Bean
    ReviewGuideFacade reviewGuideFacade(
            ReviewGuideRepository reviewGuideRepository,
            ReviewGuideMinJumpRatingLoader reviewGuideMinJumpRatingLoader
    ) {
        return new ReviewGuideFacade(reviewGuideRepository, reviewGuideMinJumpRatingLoader);
    }
}
