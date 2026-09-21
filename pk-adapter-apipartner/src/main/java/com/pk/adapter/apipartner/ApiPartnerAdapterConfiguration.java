package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.auth.port.LenderUserDisablePort;
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
@EnableConfigurationProperties(ApiPartnerProperties.class)
public class ApiPartnerAdapterConfiguration {
    @Bean
    @ConditionalOnMissingBean(CreditCallbackParser.class)
    CreditCallbackParser apiPartnerCreditCallbackParser(ObjectMapper objectMapper) {
        return new ApiPartnerCreditCallbackParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCallbackParser.class)
    LoanCallbackParser apiPartnerLoanCallbackParser(ObjectMapper objectMapper) {
        return new ApiPartnerLoanCallbackParser(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(ServerEventCallbackParser.class)
    ServerEventCallbackParser apiPartnerServerEventCallbackParser(ObjectMapper objectMapper) {
        return new ApiPartnerServerEventCallbackParser(objectMapper);
    }

    @Bean
    ApiPartnerHttpStack apiPartnerHttpStack(
            ApiPartnerProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Autowired(required = false) PlatformStructuredLogger structuredLogger
    ) {
        return ApiPartnerHttpStack.create(properties, interactionLogRepository, objectMapper, structuredLogger);
    }

    @Bean
    LenderBankPort lenderBankPort(ApiPartnerHttpStack httpStack) {
        if (httpStack.enabled()) {
            return new ApiPartnerBankAdapter(httpStack.requireHttpClient());
        }
        return new FakeApiPartnerBankAdapter();
    }

    @Bean
    LenderAreaPort lenderAreaPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerAreaAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerAreaAdapter();
    }

    @Bean
    LenderProfileSyncPort lenderProfileSyncPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerProfileSyncAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerProfileSyncAdapter();
    }

    @Bean
    LenderProfileQueryPort lenderProfileQueryPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerProfileQueryAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerProfileQueryAdapter();
    }

    @Bean
    LenderBankCardPort lenderBankCardPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerBankCardAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerBankCardAdapter();
    }

    @Bean
    LenderCreditPort lenderCreditPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerCreditAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerCreditAdapter();
    }

    @Bean
    LenderUserStatusPort lenderUserStatusPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerUserStatusAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerUserStatusAdapter();
    }

    @Bean
    LenderUserDisablePort lenderUserDisablePort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerUserDisableAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerUserDisableAdapter();
    }

    @Bean
    LenderLoanProductPort lenderLoanProductPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanProductAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanProductAdapter();
    }

    @Bean
    LenderLoanTrialPort lenderLoanTrialPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanTrialAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanTrialAdapter(objectMapper);
    }

    @Bean
    LenderLoanApplyPort lenderLoanApplyPort(ApiPartnerHttpStack httpStack) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanApplyAdapter(httpStack.requireHttpClient());
        }
        return new FakeApiPartnerLoanApplyAdapter();
    }

    @Bean
    LenderLoanStatusPort lenderLoanStatusPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanStatusAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanStatusAdapter();
    }

    @Bean
    LenderLoanHistoryPort lenderLoanHistoryPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanHistoryAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanHistoryAdapter();
    }

    @Bean
    LenderLoanBillListPort lenderLoanBillListPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanBillListAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanBillListAdapter();
    }

    @Bean
    LenderLoanContractPort lenderLoanContractPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerLoanContractAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerLoanContractAdapter();
    }

    @Bean
    LenderRepayPlanPort lenderRepayPlanPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerRepayPlanAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerRepayPlanAdapter();
    }

    @Bean
    LenderRepayVaPort lenderRepayVaPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerRepayVaAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerRepayVaAdapter();
    }

    @Bean
    LenderRepayTrialPort lenderRepayTrialPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerRepayTrialAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerRepayTrialAdapter();
    }

    @Bean
    LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort(ApiPartnerHttpStack httpStack, ObjectMapper objectMapper) {
        if (httpStack.enabled()) {
            return new ApiPartnerRepayCurrentOrderAdapter(httpStack.requireHttpClient(), objectMapper);
        }
        return new FakeApiPartnerRepayCurrentOrderAdapter();
    }

    @Bean
    LenderTrackingPort lenderTrackingPort(
            ApiPartnerHttpStack httpStack,
            ApiPartnerProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Autowired(required = false) PlatformStructuredLogger structuredLogger
    ) {
        if (httpStack.enabled()) {
            return new ApiPartnerTrackingAdapter(properties, interactionLogRepository, objectMapper, structuredLogger);
        }
        return new FakeApiPartnerTrackingAdapter();
    }
}
