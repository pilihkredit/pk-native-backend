package com.pk.infra.push;

import com.pk.core.push.port.InboxMessageRepository;
import com.pk.core.push.port.PushDeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PushInfraConfiguration {
    @Bean
    PushDeviceFacade pushDeviceFacade(PushDeviceRepository pushDeviceRepository) {
        return new PushDeviceFacade(pushDeviceRepository);
    }

    @Bean
    InboxMessageFacade inboxMessageFacade(InboxMessageRepository inboxMessageRepository) {
        return new InboxMessageFacade(inboxMessageRepository);
    }
}
