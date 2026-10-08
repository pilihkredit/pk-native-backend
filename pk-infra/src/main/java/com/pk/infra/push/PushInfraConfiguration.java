package com.pk.infra.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.push.port.FcmPushPort;
import com.pk.core.push.port.InboxMessageRepository;
import com.pk.core.push.port.PushAudienceRepository;
import com.pk.core.push.port.PushDeviceRepository;
import com.pk.core.push.port.PushNotificationTaskRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    PushPayloadAssembler pushPayloadAssembler() {
        return new PushPayloadAssembler();
    }

    @Bean
    PushDeliveryFacade pushDeliveryFacade(
            FcmPushPort fcmPushPort,
            PushDeviceRepository pushDeviceRepository,
            PushAudienceRepository pushAudienceRepository,
            PushNotificationTaskRepository pushNotificationTaskRepository,
            PushPayloadAssembler pushPayloadAssembler,
            @Value("${pk.push.delivery-concurrency:4}") int sendConcurrency
    ) {
        return new PushDeliveryFacade(
                fcmPushPort,
                pushDeviceRepository,
                pushAudienceRepository,
                pushNotificationTaskRepository,
                pushPayloadAssembler,
                sendConcurrency
        );
    }
}
