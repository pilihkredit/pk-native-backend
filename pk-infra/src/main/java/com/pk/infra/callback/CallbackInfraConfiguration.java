package com.pk.infra.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.credit.CreditApplyProperties;
import com.pk.infra.credit.CreditCallbackHandler;
import com.pk.infra.credit.CreditCallbackIntakeFacade;
import com.pk.infra.credit.CreditCallbackOutboxPublisher;
import com.pk.infra.credit.CreditLenderStatusApplier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CallbackProperties.class)
public class CallbackInfraConfiguration {
    @Bean
    CreditCallbackOutboxPublisher creditCallbackOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        return new CreditCallbackOutboxPublisher(outboxEventRepository, objectMapper);
    }

    @Bean
    CreditLenderStatusApplier creditLenderStatusApplier(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            CreditApplyProperties creditApplyProperties
    ) {
        return new CreditLenderStatusApplier(
                creditApplicationRepository,
                creditStatusHistoryRepository,
                creditLimitSnapshotRepository,
                creditApplyProperties
        );
    }

    @Bean
    CreditCallbackIntakeFacade creditCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            CreditCallbackOutboxPublisher creditCallbackOutboxPublisher,
            CreditCallbackParser creditCallbackParser
    ) {
        return new CreditCallbackIntakeFacade(
                callbackEventRepository,
                creditCallbackOutboxPublisher,
                creditCallbackParser
        );
    }

    @Bean
    CreditCallbackHandler creditCallbackHandler(
            CallbackEventRepository callbackEventRepository,
            CreditApplicationRepository creditApplicationRepository,
            CreditCallbackParser creditCallbackParser,
            CreditLenderStatusApplier creditLenderStatusApplier
    ) {
        return new CreditCallbackHandler(
                callbackEventRepository,
                creditApplicationRepository,
                creditCallbackParser,
                creditLenderStatusApplier
        );
    }
}
