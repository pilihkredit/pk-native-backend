package com.pk.infra.callback;

import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LenderServerEventCallbackRepository;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.infra.credit.CreditCallbackIntakeFacade;
import com.pk.infra.credit.CreditLenderStatusApplier;
import com.pk.infra.loan.LoanCallbackIntakeFacade;
import com.pk.infra.loan.LoanLenderStatusApplier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CallbackProperties.class)
public class CallbackInfraConfiguration {
    @Bean
    CreditLenderStatusApplier creditLenderStatusApplier(
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository
    ) {
        return new CreditLenderStatusApplier(creditLenderStatusQueryRepository);
    }

    @Bean
    CreditCallbackIntakeFacade creditCallbackIntakeFacade(
            ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository,
            CreditCallbackParser creditCallbackParser,
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusApplier creditLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        return new CreditCallbackIntakeFacade(
                externalInteractionCallbackLogRepository,
                creditCallbackParser,
                creditApplicationRepository,
                creditLenderStatusApplier,
                userAuthRepository
        );
    }

    @Bean
    LoanCallbackIntakeFacade loanCallbackIntakeFacade(
            ExternalInteractionCallbackLogRepository externalInteractionCallbackLogRepository,
            LoanCallbackParser loanCallbackParser,
            LoanApplicationRepository loanApplicationRepository,
            LoanLenderStatusApplier loanLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        return new LoanCallbackIntakeFacade(
                externalInteractionCallbackLogRepository,
                loanCallbackParser,
                loanApplicationRepository,
                loanLenderStatusApplier,
                userAuthRepository
        );
    }

    @Bean
    ServerEventCallbackIntakeFacade serverEventCallbackIntakeFacade(
            LenderServerEventCallbackRepository lenderServerEventCallbackRepository,
            ServerEventCallbackParser serverEventCallbackParser,
            AppsFlyerS2sReporter appsFlyerS2sReporter
    ) {
        return new ServerEventCallbackIntakeFacade(
                lenderServerEventCallbackRepository,
                serverEventCallbackParser,
                appsFlyerS2sReporter
        );
    }
}
