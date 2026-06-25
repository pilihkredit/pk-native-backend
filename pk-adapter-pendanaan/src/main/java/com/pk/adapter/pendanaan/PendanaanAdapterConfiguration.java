package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.LenderBankPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PendanaanProperties.class)
public class PendanaanAdapterConfiguration {
    @Bean
    @ConditionalOnMissingBean(LenderBankPort.class)
    LenderBankPort fakeLenderBankPort() {
        return new FakePendanaanBankAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderAreaPort.class)
    LenderAreaPort fakeLenderAreaPort() {
        return new FakePendanaanAreaAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderProfileSyncPort.class)
    LenderProfileSyncPort fakeLenderProfileSyncPort() {
        return new FakePendanaanProfileSyncAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderCreditPort.class)
    LenderCreditPort fakeLenderCreditPort() {
        return new FakePendanaanCreditAdapter();
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderCreditPort pendanaanCreditAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanCreditAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    PendanaanOAuthTokenProvider pendanaanOAuthTokenProvider(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return new PendanaanOAuthTokenProvider(properties, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    PendanaanHttpClient pendanaanHttpClient(
            PendanaanProperties properties,
            PendanaanOAuthTokenProvider tokenProvider,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return new PendanaanHttpClient(properties, tokenProvider, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderBankPort pendanaanBankAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanBankAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderAreaPort pendanaanAreaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanAreaAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderProfileSyncPort pendanaanProfileSyncAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanProfileSyncAdapter(httpClient, objectMapper);
    }
}
