package com.pk.infra.review;

import com.pk.core.review.port.ReviewGuideRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewGuideInfraConfiguration {
    @Bean
    ReviewGuideFacade reviewGuideFacade(ReviewGuideRepository reviewGuideRepository) {
        return new ReviewGuideFacade(reviewGuideRepository);
    }
}
