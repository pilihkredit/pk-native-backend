package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LenderLoanHistoryPort;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.repay.port.LenderLoanBillListPort;
import com.pk.core.profile.port.LenderBankCardPort;
import com.pk.core.profile.port.LenderProfileQueryPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.reference.port.LenderAreaPort;
import com.pk.core.reference.port.LenderBankPort;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.tracking.port.LenderTrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnMissingBean(ServerEventCallbackParser.class)
    ServerEventCallbackParser pendanaanServerEventCallbackParser(ObjectMapper objectMapper) {
        return new PendanaanServerEventCallbackParser(objectMapper);
    }

    @Bean
    PendanaanHttpStack pendanaanHttpStack(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Autowired(required = false) PlatformStructuredLogger structuredLogger
    ) {
        return PendanaanHttpStack.create(properties, interactionLogRepository, objectMapper, structuredLogger);
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
    LenderProfileQueryPort lenderProfileQueryPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanProfileQueryAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanProfileQueryAdapter();
    }

    @Bean
    LenderBankCardPort lenderBankCardPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanBankCardAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanBankCardAdapter();
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
    LenderLoanApplyPort lenderLoanApplyPort(PendanaanHttpStack httpStack) {
        if (httpStack.enabled()) {
            return new PendanaanLoanApplyAdapter(httpStack.requireHttpClient());
        }
        return new FakePendanaanLoanApplyAdapter();
    }

    @Bean
    LenderLoanStatusPort lenderLoanStatusPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanStatusAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanStatusAdapter();
    }

    @Bean
    LenderLoanHistoryPort lenderLoanHistoryPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanHistoryAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanHistoryAdapter();
    }

    @Bean
    LenderLoanBillListPort lenderLoanBillListPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanBillListAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanBillListAdapter();
    }

    @Bean
    LenderLoanContractPort lenderLoanContractPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanLoanContractAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanLoanContractAdapter();
    }

    @Bean
    LenderRepayPlanPort lenderRepayPlanPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanRepayPlanAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanRepayPlanAdapter();
    }

    @Bean
    LenderRepayVaPort lenderRepayVaPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanRepayVaAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanRepayVaAdapter();
    }

    @Bean
    LenderRepayTrialPort lenderRepayTrialPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanRepayTrialAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanRepayTrialAdapter();
    }

    @Bean
    LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort(PendanaanHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new PendanaanRepayCurrentOrderAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakePendanaanRepayCurrentOrderAdapter();
    }

    @Bean
    LenderTrackingPort lenderTrackingPort(
            PendanaanHttpStack httpStack,
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Autowired(required = false) PlatformStructuredLogger structuredLogger
    ) {
        if (httpStack.enabled()) {
            return new PendanaanTrackingAdapter(properties, interactionLogRepository, objectMapper, structuredLogger);
        }
        return new FakePendanaanTrackingAdapter();
    }
}
