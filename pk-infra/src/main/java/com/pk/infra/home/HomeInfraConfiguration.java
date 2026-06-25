package com.pk.infra.home;

import com.pk.core.home.port.HomeLifecycleReadRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HomeInfraConfiguration {
    @Bean
    HomeSummaryFacade homeSummaryFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            HomeLifecycleReadRepository homeLifecycleReadRepository
    ) {
        return new HomeSummaryFacade(onboardingProgressFacade, homeLifecycleReadRepository);
    }
}
