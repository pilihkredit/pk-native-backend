package com.pk.infra.launch;

import com.pk.core.launch.port.AppLaunchRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppLaunchInfraConfiguration {
    @Bean
    AppLaunchFacade appLaunchFacade(AppLaunchRepository appLaunchRepository) {
        return new AppLaunchFacade(appLaunchRepository);
    }
}
