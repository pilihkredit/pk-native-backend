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
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.core.review.ReviewSandboxLoanDataPort;
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
    ReviewSandboxAdapters reviewSandboxAdapters(
            ReviewSandboxConfigPort configPort,
            ReviewSandboxLoanDataPort loanDataPort,
            ObjectMapper objectMapper
    ) {
        return new ReviewSandboxAdapters(configPort, loanDataPort, objectMapper);
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
    LenderProfileSyncPort lenderProfileSyncPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderProfileSyncPort live = httpStack.enabled()
                ? new PendanaanProfileSyncAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanProfileSyncAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderProfileSyncPort.class, live, review.profileSync(), configPort);
    }

    @Bean
    LenderProfileQueryPort lenderProfileQueryPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderProfileQueryPort live = httpStack.enabled()
                ? new PendanaanProfileQueryAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanProfileQueryAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderProfileQueryPort.class, live, review.profileQuery(), configPort);
    }

    @Bean
    LenderBankCardPort lenderBankCardPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderBankCardPort live = httpStack.enabled()
                ? new PendanaanBankCardAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanBankCardAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderBankCardPort.class, live, review.bankCard(), configPort);
    }

    @Bean
    LenderCreditPort lenderCreditPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderCreditPort live = httpStack.enabled()
                ? new PendanaanCreditAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanCreditAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderCreditPort.class, live, review.credit(), configPort);
    }

    @Bean
    LenderUserStatusPort lenderUserStatusPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderUserStatusPort live = httpStack.enabled()
                ? new PendanaanUserStatusAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanUserStatusAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderUserStatusPort.class, live, review.userStatus(), configPort);
    }

    @Bean
    LenderLoanProductPort lenderLoanProductPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanProductPort live = httpStack.enabled()
                ? new PendanaanLoanProductAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanProductAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanProductPort.class, live, review.products(), configPort);
    }

    @Bean
    LenderLoanTrialPort lenderLoanTrialPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanTrialPort live = httpStack.enabled()
                ? new PendanaanLoanTrialAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanTrialAdapter(objectMapper);
        return ReviewSandboxRoutingProxy.wrap(LenderLoanTrialPort.class, live, review.loanTrial(), configPort);
    }

    @Bean
    LenderLoanApplyPort lenderLoanApplyPort(
            PendanaanHttpStack httpStack,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanApplyPort live = httpStack.enabled()
                ? new PendanaanLoanApplyAdapter(httpStack.requireHttpClient())
                : new FakePendanaanLoanApplyAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanApplyPort.class, live, review.loanApply(), configPort);
    }

    @Bean
    LenderLoanStatusPort lenderLoanStatusPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanStatusPort live = httpStack.enabled()
                ? new PendanaanLoanStatusAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanStatusAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanStatusPort.class, live, review.loanStatus(), configPort);
    }

    @Bean
    LenderLoanHistoryPort lenderLoanHistoryPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanHistoryPort live = httpStack.enabled()
                ? new PendanaanLoanHistoryAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanHistoryAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanHistoryPort.class, live, review.loanHistory(), configPort);
    }

    @Bean
    LenderLoanBillListPort lenderLoanBillListPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanBillListPort live = httpStack.enabled()
                ? new PendanaanLoanBillListAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanBillListAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanBillListPort.class, live, review.bills(), configPort);
    }

    @Bean
    LenderLoanContractPort lenderLoanContractPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderLoanContractPort live = httpStack.enabled()
                ? new PendanaanLoanContractAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanLoanContractAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderLoanContractPort.class, live, review.loanContracts(), configPort);
    }

    @Bean
    LenderRepayPlanPort lenderRepayPlanPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderRepayPlanPort live = httpStack.enabled()
                ? new PendanaanRepayPlanAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanRepayPlanAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderRepayPlanPort.class, live, review.repayPlan(), configPort);
    }

    @Bean
    LenderRepayVaPort lenderRepayVaPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderRepayVaPort live = httpStack.enabled()
                ? new PendanaanRepayVaAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanRepayVaAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderRepayVaPort.class, live, review.repayVa(), configPort);
    }

    @Bean
    LenderRepayTrialPort lenderRepayTrialPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderRepayTrialPort live = httpStack.enabled()
                ? new PendanaanRepayTrialAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanRepayTrialAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderRepayTrialPort.class, live, review.repayTrial(), configPort);
    }

    @Bean
    LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort(
            PendanaanHttpStack httpStack,
            ObjectMapper objectMapper,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderRepayCurrentOrderPort live = httpStack.enabled()
                ? new PendanaanRepayCurrentOrderAdapter(httpStack.requireHttpClient(), objectMapper)
                : new FakePendanaanRepayCurrentOrderAdapter();
        return ReviewSandboxRoutingProxy.wrap(
                LenderRepayCurrentOrderPort.class,
                live,
                review.repayCurrentOrder(),
                configPort
        );
    }

    @Bean
    LenderTrackingPort lenderTrackingPort(
            PendanaanHttpStack httpStack,
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Autowired(required = false) PlatformStructuredLogger structuredLogger,
            ReviewSandboxAdapters review,
            ReviewSandboxConfigPort configPort
    ) {
        LenderTrackingPort live = httpStack.enabled()
                ? new PendanaanTrackingAdapter(properties, interactionLogRepository, objectMapper, structuredLogger)
                : new FakePendanaanTrackingAdapter();
        return ReviewSandboxRoutingProxy.wrap(LenderTrackingPort.class, live, review.tracking(), configPort);
    }
}
