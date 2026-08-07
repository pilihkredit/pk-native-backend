package com.pk.infra.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LenderLoanHistoryPort;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderHistoryOrderRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.loan.port.ContractFileRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.core.loan.port.LenderProductListRepository;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties({
        LoanProductProperties.class,
        LoanQuoteProperties.class,
        LoanApplyProperties.class,
        LoanStatusBackfillProperties.class
})
public class LoanInfraConfiguration {
    @Bean
    ProductListCache productListCache(
            StringRedisTemplate redisTemplate,
            LoanProductProperties loanProductProperties
    ) {
        return new RedisProductListCache(redisTemplate, loanProductProperties);
    }

    @Bean
    ProductListResolver productListResolver(
            LenderProductListRepository lenderProductListRepository,
            ProductListCache productListCache,
            LenderLoanProductPort lenderLoanProductPort,
            LoanProductProperties loanProductProperties
    ) {
        return new ProductListResolver(
                lenderProductListRepository,
                productListCache,
                lenderLoanProductPort,
                loanProductProperties
        );
    }

    @Bean
    LoanProductFacade loanProductFacade(
            CreditApplicationRepository creditApplicationRepository,
            ProductListResolver productListResolver
    ) {
        return new LoanProductFacade(creditApplicationRepository, productListResolver);
    }

    @Bean
    LoanTrialFacade loanTrialFacade(
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            LoanProductFacade loanProductFacade,
            LenderLoanTrialPort lenderLoanTrialPort,
            LoanQuoteRepository loanQuoteRepository,
            LoanQuoteProperties loanQuoteProperties
    ) {
        return new LoanTrialFacade(
                creditApplicationRepository,
                creditLenderStatusQueryRepository,
                loanProductFacade,
                lenderLoanTrialPort,
                loanQuoteRepository,
                loanQuoteProperties
        );
    }

    @Bean
    LoanApplyOutboxPublisher loanApplyOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        return new LoanApplyOutboxPublisher(outboxEventRepository, objectMapper);
    }

    @Bean
    LoanStatusPollHandler loanStatusPollHandler(
            LenderLoanStatusPort lenderLoanStatusPort,
            LoanLenderStatusApplier loanLenderStatusApplier,
            com.pk.core.auth.port.UserAuthRepository userAuthRepository
    ) {
        return new LoanStatusPollHandler(lenderLoanStatusPort, loanLenderStatusApplier, userAuthRepository);
    }

    @Bean
    LoanStatusBackfillService loanStatusBackfillService(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusPollHandler loanStatusPollHandler,
            LoanStatusBackfillProperties loanStatusBackfillProperties
    ) {
        return new LoanStatusBackfillService(
                loanApplicationRepository,
                loanStatusPollHandler,
                loanStatusBackfillProperties
        );
    }

    @Bean
    LoanApplyFacade loanApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            LoanQuoteRepository loanQuoteRepository,
            ProfileVersionRepository profileVersionRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanLenderStatusQueryRepository loanLenderStatusQueryRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanApplyHandler loanApplyHandler,
            LoanApplyProperties loanApplyProperties,
            LoanApplyOutboxPublisher loanApplyOutboxPublisher,
            LoanStatusPollHandler loanStatusPollHandler
    ) {
        return new LoanApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                loanQuoteRepository,
                profileVersionRepository,
                loanApplicationRepository,
                loanLenderStatusQueryRepository,
                loanStatusHistoryRepository,
                loanApplyHandler,
                loanApplyProperties,
                loanApplyOutboxPublisher,
                loanStatusPollHandler
        );
    }

    @Bean
    LoanApplyHandler loanApplyHandler(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LenderLoanApplyPort lenderLoanApplyPort,
            LoanApplyProperties loanApplyProperties
    ) {
        return new LoanApplyHandler(
                loanApplicationRepository,
                loanStatusHistoryRepository,
                lenderLoanApplyPort,
                loanApplyProperties
        );
    }

    @Bean
    LoanLenderStatusApplier loanLenderStatusApplier(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanLenderStatusQueryRepository loanLenderStatusQueryRepository,
            LoanApplyProperties loanApplyProperties
    ) {
        return new LoanLenderStatusApplier(
                loanApplicationRepository,
                loanStatusHistoryRepository,
                loanLenderStatusQueryRepository,
                loanApplyProperties
        );
    }

    @Bean
    LoanHistoryFacade loanHistoryFacade(
            LenderLoanHistoryPort lenderLoanHistoryPort,
            LoanLenderHistoryOrderRepository loanLenderHistoryOrderRepository
    ) {
        return new LoanHistoryFacade(lenderLoanHistoryPort, loanLenderHistoryOrderRepository);
    }

    @Bean
    LoanContractFacade loanContractFacade(
            LoanApplicationRepository loanApplicationRepository,
            LenderLoanContractPort lenderLoanContractPort,
            ContractFileRepository contractFileRepository
    ) {
        return new LoanContractFacade(
                loanApplicationRepository,
                lenderLoanContractPort,
                contractFileRepository
        );
    }
}
