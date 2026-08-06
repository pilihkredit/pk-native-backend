package com.pk.infra.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.push.port.FcmPushPort;
import com.pk.core.push.port.InboxMessageRepository;
import com.pk.core.push.port.PushDeviceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FcmProperties.class)
public class PushInfraConfiguration {
    @Bean
    PushDeviceFacade pushDeviceFacade(PushDeviceRepository pushDeviceRepository) {
        return new PushDeviceFacade(pushDeviceRepository);
    }

    @Bean
    InboxMessageFacade inboxMessageFacade(InboxMessageRepository inboxMessageRepository) {
        return new InboxMessageFacade(inboxMessageRepository);
    }

    @Bean
    @ConditionalOnMissingBean(GoogleServiceAccountTokenProvider.class)
    GoogleServiceAccountTokenProvider googleServiceAccountTokenProvider(
            FcmProperties fcmProperties,
            ObjectMapper objectMapper
    ) {
        return new GoogleServiceAccountTokenProvider(fcmProperties, objectMapper);
    }

    @Bean
    FcmPushPort fcmPushPort(
            FcmProperties fcmProperties,
            GoogleServiceAccountTokenProvider tokenProvider,
            ObjectMapper objectMapper
    ) {
        if (fcmProperties.enabled() && fcmProperties.configured()) {
            return new GoogleFcmPushAdapter(fcmProperties, tokenProvider, objectMapper);
        }
        return new DisabledFcmPushAdapter();
    }
}
