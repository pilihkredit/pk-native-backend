package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.callback.port.CreditCallbackParser;
import com.pk.core.callback.port.LoanCallbackParser;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.credit.port.LenderCreditPort;
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

@Configuration
@EnableConfigurationProperties(PendanaanProperties.class)
public class PendanaanAdapterConfiguration {
    @Bean
    @ConditionalOnMissingBean(LenderBankPort.class)
    LenderBankPort fakeLenderBankPort() {
        return new FakePendanaanBankAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderAreaPort.class)
    LenderAreaPort fakeLenderAreaPort() {
        return new FakePendanaanAreaAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderProfileSyncPort.class)
    LenderProfileSyncPort fakeLenderProfileSyncPort() {
        return new FakePendanaanProfileSyncAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderCreditPort.class)
    LenderCreditPort fakeLenderCreditPort() {
        return new FakePendanaanCreditAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderLoanProductPort.class)
    LenderLoanProductPort fakeLenderLoanProductPort() {
        return new FakePendanaanLoanProductAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(LenderLoanTrialPort.class)
    LenderLoanTrialPort fakeLenderLoanTrialPort(ObjectMapper objectMapper) {
        return new FakePendanaanLoanTrialAdapter(objectMapper);
    }

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
    @ConditionalOnPendanaanHttpEnabled
    LenderCreditPort pendanaanCreditAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanCreditAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderLoanProductPort pendanaanLoanProductAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanLoanProductAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderLoanTrialPort pendanaanLoanTrialAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanLoanTrialAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderLoanApplyPort pendanaanLoanApplyAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanLoanApplyAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderLoanStatusPort pendanaanLoanStatusAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanLoanStatusAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderLoanContractPort pendanaanLoanContractAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanLoanContractAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderRepayPlanPort pendanaanRepayPlanAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanRepayPlanAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderRepayVaPort pendanaanRepayVaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanRepayVaAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderRepayTrialPort pendanaanRepayTrialAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanRepayTrialAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderRepayCurrentOrderPort pendanaanRepayCurrentOrderAdapter(
            PendanaanHttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        return new PendanaanRepayCurrentOrderAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    PendanaanOAuthTokenProvider pendanaanOAuthTokenProvider(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return new PendanaanOAuthTokenProvider(properties, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    PendanaanHttpClient pendanaanHttpClient(
            PendanaanProperties properties,
            PendanaanOAuthTokenProvider tokenProvider,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        return new PendanaanHttpClient(properties, tokenProvider, interactionLogRepository, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderBankPort pendanaanBankAdapter(PendanaanHttpClient httpClient) {
        return new PendanaanBankAdapter(httpClient);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderAreaPort pendanaanAreaAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanAreaAdapter(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnPendanaanHttpEnabled
    LenderProfileSyncPort pendanaanProfileSyncAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        return new PendanaanProfileSyncAdapter(httpClient, objectMapper);
    }
}
