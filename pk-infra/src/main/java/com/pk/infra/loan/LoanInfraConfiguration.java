package com.pk.infra.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.ProductListCache;
import com.pk.core.loan.port.ProductSnapshotRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties({LoanProductProperties.class, LoanQuoteProperties.class})
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
            ProductSnapshotPayloadCodec productSnapshotPayloadCodec,
            LoanProductProperties loanProductProperties
    ) {
        return new ProductListResolver(
                productSnapshotRepository,
                productListCache,
                lenderLoanProductPort,
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
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            LoanProductFacade loanProductFacade,
            LenderLoanTrialPort lenderLoanTrialPort,
            LoanQuoteRepository loanQuoteRepository,
            LoanQuoteProperties loanQuoteProperties
    ) {
        return new LoanTrialFacade(
                creditApplicationRepository,
                creditLimitSnapshotRepository,
                loanProductFacade,
                lenderLoanTrialPort,
                loanQuoteRepository,
                loanQuoteProperties
        );
    }
}
