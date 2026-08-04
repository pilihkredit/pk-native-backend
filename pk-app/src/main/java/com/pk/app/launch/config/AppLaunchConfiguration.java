package com.pk.app.launch.config;

import com.pk.app.launch.application.AppLaunchApplicationService;
import com.pk.infra.launch.AppLaunchFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppLaunchConfiguration {
    @Bean
    AppLaunchApplicationService appLaunchApplicationService(AppLaunchFacade appLaunchFacade) {
        return new AppLaunchApplicationService(appLaunchFacade);
    }
}
