package com.pk.app.review.config;

import com.pk.app.review.application.ReviewGuideApplicationService;
import com.pk.infra.review.ReviewGuideFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewGuideConfiguration {
    @Bean
    ReviewGuideApplicationService reviewGuideApplicationService(ReviewGuideFacade reviewGuideFacade) {
        return new ReviewGuideApplicationService(reviewGuideFacade);
    }
}
