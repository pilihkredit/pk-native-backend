package com.pk.infra.attribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.attribution.port.AdjustConfigRepository;
import com.pk.core.attribution.port.AdjustEventRecordRepository;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttributionInfraConfiguration {
    @Bean
    AppsFlyerS2sReporter appsFlyerS2sReporter(
            AdjustConfigRepository adjustConfigRepository,
            AdjustEventRecordRepository adjustEventRecordRepository,
            ProfileDeviceRepository profileDeviceRepository,
            ProfileAfRepository profileAfRepository,
            ObjectMapper objectMapper
    ) {
        return new AppsFlyerS2sReporterImpl(
                adjustConfigRepository,
                adjustEventRecordRepository,
                profileDeviceRepository,
                profileAfRepository,
                objectMapper
        );
    }
}
