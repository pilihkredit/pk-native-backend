package com.pk.infra.attribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.attribution.port.AdjustConfigRepository;
import com.pk.core.attribution.port.AdjustEventConfigRepository;
import com.pk.core.attribution.port.AdjustEventRecordRepository;
import com.pk.core.attribution.port.AppConfRepository;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.profile.port.ProfileDeviceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttributionInfraConfiguration {
    @Bean
    AppsFlyerS2sReporter appsFlyerS2sReporter(
            AppConfRepository appConfRepository,
            AdjustConfigRepository adjustConfigRepository,
            AdjustEventConfigRepository adjustEventConfigRepository,
            AdjustEventRecordRepository adjustEventRecordRepository,
            ProfileDeviceRepository profileDeviceRepository,
            ObjectMapper objectMapper
    ) {
        return new AppsFlyerS2sReporterImpl(
                appConfRepository,
                adjustConfigRepository,
                adjustEventConfigRepository,
                adjustEventRecordRepository,
                profileDeviceRepository,
                objectMapper
        );
    }
}
