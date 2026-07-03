package com.pk.infra.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.loan.port.ContractFileRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.core.loan.port.ProductSnapshotRepository;
import com.pk.core.loan.port.LenderProductLatestRepository;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties({
        LoanProductProperties.class,
        LoanQuoteProperties.class,
        LoanApplyProperties.class
})
public class LoanInfraConfiguration {
    @Bean
    ProductSnapshotPayloadCodec productSnapshotPayloadCodec(ObjectMapper objectMapper) {
        return new ProductSnapshotPayloadCodec(objectMapper);
    }

    @Bean
    ProductListCache productListCache(
            StringRedisTemplate redisTemplate,
            LoanProductProperties loanProductProperties
    ) {
        return new RedisProductListCache(redisTemplate, loanProductProperties);
    }

    @Bean
    ProductListResolver productListResolver(
            ProductSnapshotRepository productSnapshotRepository,
            ProductListCache productListCache,
            LenderLoanProductPort lenderLoanProductPort,
            LenderProductLatestRepository lenderProductLatestRepository,
            ProductSnapshotPayloadCodec productSnapshotPayloadCodec,
            LoanProductProperties loanProductProperties
    ) {
        return new ProductListResolver(
                productSnapshotRepository,
                productListCache,
                lenderLoanProductPort,
                lenderProductLatestRepository,
                productSnapshotPayloadCodec,
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
    LoanApplyFacade loanApplyFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            LoanQuoteRepository loanQuoteRepository,
            LoanQuoteProperties loanQuoteProperties,
            LoanProductFacade loanProductFacade,
            ProfileVersionRepository profileVersionRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanApplyOutboxPublisher loanApplyOutboxPublisher
    ) {
        return new LoanApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                creditLenderStatusQueryRepository,
                loanQuoteRepository,
                loanQuoteProperties,
                loanProductFacade,
                profileVersionRepository,
                loanApplicationRepository,
                loanStatusHistoryRepository,
                loanApplyOutboxPublisher
        );
    }

    @Bean
    @ConditionalOnBean(LenderLoanApplyPort.class)
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
            LoanApplyProperties loanApplyProperties
    ) {
        return new LoanLenderStatusApplier(
                loanApplicationRepository,
                loanStatusHistoryRepository,
                loanApplyProperties
        );
    }

    @Bean
    @ConditionalOnBean(LenderLoanStatusPort.class)
    LoanStatusPollHandler loanStatusPollHandler(
            LenderLoanStatusPort lenderLoanStatusPort,
            LoanLenderStatusApplier loanLenderStatusApplier
    ) {
        return new LoanStatusPollHandler(lenderLoanStatusPort, loanLenderStatusApplier);
    }

    @Bean
    @ConditionalOnBean(LenderLoanContractPort.class)
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
