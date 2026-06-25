package com.pk.infra.repay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepayVaSnapshotRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties(RepayTrialProperties.class)
public class RepayInfraConfiguration {
    @Bean
    LoanBillsFacade loanBillsFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository
    ) {
        return new LoanBillsFacade(loanBillReadRepository, repaymentPlanTermRepository);
    }

    @Bean
    RepayPlanFacade repayPlanFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            LenderRepayPlanPort lenderRepayPlanPort
    ) {
        return new RepayPlanFacade(loanBillReadRepository, repaymentPlanTermRepository, lenderRepayPlanPort);
    }

    @Bean
    RepayBillsOverviewFacade repayBillsOverviewFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            RepayCurrentOrderRepository repayCurrentOrderRepository,
            ObjectMapper objectMapper
    ) {
        return new RepayBillsOverviewFacade(
                loanBillReadRepository,
                repaymentPlanTermRepository,
                repayCurrentOrderRepository,
                objectMapper
        );
    }

    @Bean
    RepayVaFacade repayVaFacade(
            LenderRepayVaPort lenderRepayVaPort,
            RepayVaSnapshotRepository repayVaSnapshotRepository,
            ObjectMapper objectMapper
    ) {
        return new RepayVaFacade(lenderRepayVaPort, repayVaSnapshotRepository, objectMapper);
    }

    @Bean
    RepayTrialFacade repayTrialFacade(
            LoanBillReadRepository loanBillReadRepository,
            LenderRepayTrialPort lenderRepayTrialPort,
            RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository,
            RepayTrialProperties repayTrialProperties,
            ObjectMapper objectMapper
    ) {
        return new RepayTrialFacade(
                loanBillReadRepository,
                lenderRepayTrialPort,
                repaymentTrialSnapshotRepository,
                repayTrialProperties,
                objectMapper
        );
    }

    @Bean
    RepayCurrentOrderFacade repayCurrentOrderFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentTrialSnapshotRepository repaymentTrialSnapshotRepository,
            LenderRepayCurrentOrderPort lenderRepayCurrentOrderPort,
            RepayCurrentOrderRepository repayCurrentOrderRepository,
            ObjectMapper objectMapper
    ) {
        return new RepayCurrentOrderFacade(
                loanBillReadRepository,
                repaymentTrialSnapshotRepository,
                lenderRepayCurrentOrderPort,
                repayCurrentOrderRepository,
                objectMapper
        );
    }
}
