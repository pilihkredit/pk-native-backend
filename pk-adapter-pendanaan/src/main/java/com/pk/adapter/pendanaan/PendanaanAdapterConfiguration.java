package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.LenderBankPort;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LenderRepayVaPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@EnableConfigurationProperties(PendanaanProperties.class)
public class PendanaanAdapterConfiguration {
    @Bean
    @ConditionalOnMissingBean(CreditCallbackParser.class)
    CreditCallbackParser pendanaanCreditCallbackParser(ObjectMapper objectMapper) {
        return new PendanaanCreditCallbackParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCallbackParser.class)
    LoanCallbackParser pendanaanLoanCallbackParser(ObjectMapper objectMapper) {
        return new PendanaanLoanCallbackParser(objectMapper);
    }

    @Bean
    PendanaanHttpStack pendanaanHttpStack(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return PendanaanHttpStack.create(properties, interactionLogRepository, objectMapper);
    }

    @Bean
    LenderBankPort lenderBankPort(PendanaanHttpStack httpStack) {
        if (httpStack.enabled()) {
            return new PendanaanBankAdapter(httpStack.requireHttpClient());
        }
        return new FakePendanaanBankAdapter();
    }

    @Bean
    LenderAreaPort lenderAreaPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanAreaAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanAreaAdapter();
    }

    @Bean
    LenderProfileSyncPort lenderProfileSyncPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanProfileSyncAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanProfileSyncAdapter();
    }

    @Bean
    LenderCreditPort lenderCreditPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanCreditAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanCreditAdapter();
    }

    @Bean
    LenderUserStatusPort lenderUserStatusPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanUserStatusAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanUserStatusAdapter();
    }

    @Bean
    LenderLoanProductPort lenderLoanProductPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanProductAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanProductAdapter();
    }

    @Bean
    LenderLoanTrialPort lenderLoanTrialPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanTrialAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanTrialAdapter(objectMapper);
    }

    @Bean
    @Nullable
    LenderLoanApplyPort lenderLoanApplyPort(PendanaanHttpStack httpStack) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanLoanApplyAdapter(httpStack.requireHttpClient());
    }

    @Bean
    @Nullable
    LenderLoanStatusPort lenderLoanStatusPort(PendanaanHttpStack httpStack) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanLoanStatusAdapter(httpStack.requireHttpClient());
    }

    @Bean
    @Nullable
    LenderLoanContractPort lenderLoanContractPort(PendanaanHttpStack httpStack) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanLoanContractAdapter(httpStack.requireHttpClient());
    }

    @Bean
    @Nullable
    LenderRepayPlanPort lenderRepayPlanPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanRepayPlanAdapter(httpStack.requireHttpClient(), objectMapper);
    }

    @Bean
    @Nullable
    LenderRepayVaPort lenderRepayVaPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanRepayVaAdapter(httpStack.requireHttpClient(), objectMapper);
    }

    @Bean
    @Nullable
    LenderRepayTrialPort lenderRepayTrialPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanRepayTrialAdapter(httpStack.requireHttpClient(), objectMapper);
    }

    @Bean
    @Nullable
    LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (!httpStack.enabled()) {
            return null;
        }
        return new PendanaanRepayCurrentOrderAdapter(httpStack.requireHttpClient(), objectMapper);
    }
}
