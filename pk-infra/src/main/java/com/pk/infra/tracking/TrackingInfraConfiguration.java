package com.pk.infra.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.tracking.port.TrackingEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackingInfraConfiguration {
    @Bean
    TrackingFacade trackingFacade(
            TrackingEventRepository trackingEventRepository,
            ObjectMapper objectMapper
    ) {
        return new TrackingFacade(trackingEventRepository, objectMapper);
    }
}
