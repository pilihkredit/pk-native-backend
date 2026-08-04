package com.pk.app.push.config;

import com.pk.app.push.application.PushApplicationService;
import com.pk.infra.push.InboxMessageFacade;
import com.pk.infra.push.PushDeviceFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PushConfiguration {
    @Bean
    PushApplicationService pushApplicationService(
            PushDeviceFacade pushDeviceFacade,
            InboxMessageFacade inboxMessageFacade
    ) {
        return new PushApplicationService(pushDeviceFacade, inboxMessageFacade);
    }
}
