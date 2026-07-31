package com.pk.infra.home;

import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.home.port.UserLenderStatusQueryRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HomeInfraConfiguration {
    @Bean
    HomeSummaryFacade homeSummaryFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            LenderUserStatusPort lenderUserStatusPort,
            UserLenderStatusQueryRepository userLenderStatusQueryRepository
    ) {
        return new HomeSummaryFacade(
                onboardingProgressFacade,
                lenderUserStatusPort,
                userLenderStatusQueryRepository
        );
    }
}
