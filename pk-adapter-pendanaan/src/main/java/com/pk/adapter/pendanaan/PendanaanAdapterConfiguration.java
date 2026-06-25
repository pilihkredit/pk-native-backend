package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.LenderBankPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PendanaanProperties.class)
public class PendanaanAdapterConfiguration {
    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_FAKE, matchIfMissing = true)
    LenderBankPort fakeLenderBankPort() {
        return new FakePendanaanBankAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_FAKE, matchIfMissing = true)
    LenderAreaPort fakeLenderAreaPort() {
        return new FakePendanaanAreaAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_FAKE, matchIfMissing = true)
    LenderProfileSyncPort fakeLenderProfileSyncPort() {
        return new FakePendanaanProfileSyncAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_HTTP)
    PendanaanOAuthTokenProvider pendanaanOAuthTokenProvider(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        properties.validateHttpSettings();
        return new PendanaanOAuthTokenProvider(properties, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_HTTP)
    PendanaanHttpClient pendanaanHttpClient(
            PendanaanProperties properties,
            PendanaanOAuthTokenProvider tokenProvider,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return new PendanaanHttpClient(properties, tokenProvider, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_HTTP)
    LenderBankPort pendanaanBankAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanBankAdapter(httpClient);
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_HTTP)
    LenderAreaPort pendanaanAreaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanAreaAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "pk.lender.pendanaan.mode", havingValue = PendanaanProperties.MODE_HTTP)
    LenderProfileSyncPort pendanaanProfileSyncAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanProfileSyncAdapter(httpClient, objectMapper);
    }
}
