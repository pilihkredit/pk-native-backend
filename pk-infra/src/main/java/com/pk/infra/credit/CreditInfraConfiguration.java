package com.pk.infra.credit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.core.provider.port.PkProviderRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.beans.factory.annotation.Value;
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
            PkProviderRepository pkProviderRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            CreditApplyProperties creditApplyProperties,
            CreditApplyHandler creditApplyHandler,
            CreditApplyOutboxPublisher creditApplyOutboxPublisher,
            CreditStatusPollHandler creditStatusPollHandler,
            @Value("${pk.lender.config.provider-code:pendanaan}") String configuredProviderCode
    ) {
        return new CreditApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                pkProviderRepository,
                creditLenderStatusQueryRepository,
                creditApplyProperties,
                creditApplyHandler,
                creditApplyOutboxPublisher,
                creditStatusPollHandler,
                configuredProviderCode
        );
    }

    @Bean
    CreditAppsFlyerPreSync creditAppsFlyerPreSync(
            com.pk.infra.profile.AppsFlyerLenderPayloadResolver appsFlyerLenderPayloadResolver,
            com.pk.infra.profile.ProfileSyncOrchestrator profileSyncOrchestrator,
            CreditApplicationRepository creditApplicationRepository
    ) {
        return new CreditAppsFlyerPreSync(
                appsFlyerLenderPayloadResolver,
                profileSyncOrchestrator,
                creditApplicationRepository
        );
    }

    @Bean
    CreditApplyHandler creditApplyHandler(
            CreditApplicationRepository creditApplicationRepository,
            LenderCreditPort lenderCreditPort,
            CreditAppsFlyerPreSync creditAppsFlyerPreSync
    ) {
        return new CreditApplyHandler(
                creditApplicationRepository,
                lenderCreditPort,
                creditAppsFlyerPreSync
        );
    }

    @Bean
    CreditStatusPollHandler creditStatusPollHandler(
            LenderCreditPort lenderCreditPort,
            CreditLenderStatusApplier creditLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        return new CreditStatusPollHandler(
                lenderCreditPort,
                creditLenderStatusApplier,
                userAuthRepository
        );
    }

    @Bean
    CreditStatusBackfillService creditStatusBackfillService(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusPollHandler creditStatusPollHandler
    ) {
        return new CreditStatusBackfillService(creditApplicationRepository, creditStatusPollHandler);
    }
}
