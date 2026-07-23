package com.pk.infra.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.port.CallbackEventRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.credit.CreditCallbackHandler;
import com.pk.infra.credit.CreditCallbackIntakeFacade;
import com.pk.infra.credit.CreditCallbackOutboxPublisher;
import com.pk.infra.credit.CreditLenderStatusApplier;
import com.pk.infra.loan.LoanCallbackHandler;
import com.pk.infra.loan.LoanCallbackIntakeFacade;
import com.pk.infra.loan.LoanCallbackOutboxPublisher;
import com.pk.infra.loan.LoanLenderStatusApplier;
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
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository
    ) {
        return new CreditLenderStatusApplier(creditLenderStatusQueryRepository);
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

    @Bean
    LoanCallbackOutboxPublisher loanCallbackOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        return new LoanCallbackOutboxPublisher(outboxEventRepository, objectMapper);
    }

    @Bean
    LoanCallbackIntakeFacade loanCallbackIntakeFacade(
            ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository,
            LoanCallbackParser loanCallbackParser,
            LoanApplicationRepository loanApplicationRepository,
            LoanLenderStatusQueryRepository loanLenderStatusQueryRepository,
            LoanLenderStatusApplier loanLenderStatusApplier
    ) {
        return new LoanCallbackIntakeFacade(
                externalInteractionCallbackLogRepository,
                loanCallbackParser,
                loanApplicationRepository,
                loanLenderStatusQueryRepository,
                loanLenderStatusApplier
        );
    }

    @Bean
    LoanCallbackHandler loanCallbackHandler(
            CallbackEventRepository callbackEventRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanCallbackParser loanCallbackParser,
            LoanLenderStatusApplier loanLenderStatusApplier
    ) {
        return new LoanCallbackHandler(
                callbackEventRepository,
                loanApplicationRepository,
                loanCallbackParser,
                loanLenderStatusApplier
        );
    }

    @Bean
    ServerEventCallbackIntakeFacade serverEventCallbackIntakeFacade(
            CallbackEventRepository callbackEventRepository,
            ServerEventCallbackParser serverEventCallbackParser,
            AppsFlyerS2sReporter appsFlyerS2sReporter
    ) {
        return new ServerEventCallbackIntakeFacade(
                callbackEventRepository,
                serverEventCallbackParser,
                appsFlyerS2sReporter
        );
    }
}
