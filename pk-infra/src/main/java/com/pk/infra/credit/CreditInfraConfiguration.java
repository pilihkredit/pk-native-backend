package com.pk.infra.credit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CreditApplyProperties.class)
public class CreditInfraConfiguration {
    @Bean
    CreditApplyOutboxPublisher creditApplyOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        return new CreditApplyOutboxPublisher(outboxEventRepository, objectMapper);
    }

    @Bean
    CreditApplyFacade creditApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            ProfileVersionRepository profileVersionRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            CreditApplyProperties creditApplyProperties,
            CreditApplyHandler creditApplyHandler,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditStatusPollHandler creditStatusPollHandler
    ) {
        return new CreditApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                profileVersionRepository,
                creditLenderStatusQueryRepository,
                creditApplyProperties,
                creditApplyHandler,
                creditApplyOutboxPublisher,
                creditStatusHistoryRepository,
                creditStatusPollHandler
        );
    }

    @Bean
    CreditApplyHandler creditApplyHandler(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            LenderCreditPort lenderCreditPort,
            CreditApplyProperties creditApplyProperties
    ) {
        return new CreditApplyHandler(
                creditApplicationRepository,
                creditStatusHistoryRepository,
                lenderCreditPort,
                creditApplyProperties
        );
    }

    @Bean
    CreditStatusPollHandler creditStatusPollHandler(
            LenderCreditPort lenderCreditPort,
            CreditLenderStatusApplier creditLenderStatusApplier
    ) {
        return new CreditStatusPollHandler(
                lenderCreditPort,
                creditLenderStatusApplier
        );
    }
}
