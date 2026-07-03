package com.pk.infra.repay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.port.LenderLoanBillListPort;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.LoanLenderBillRepository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.core.repay.port.RepayVaSnapshotRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties(RepayTrialProperties.class)
public class RepayInfraConfiguration {
    @Bean
    LoanBillsFacade loanBillsFacade(
            LenderLoanBillListPort lenderLoanBillListPort,
            LoanLenderBillRepository loanLenderBillRepository
    ) {
        return new LoanBillsFacade(lenderLoanBillListPort, loanLenderBillRepository);
    }

    @Bean
    @ConditionalOnBean(LenderRepayPlanPort.class)
    RepayPlanFacade repayPlanFacade(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            LenderRepayPlanPort lenderRepayPlanPort
    ) {
        return new RepayPlanFacade(loanBillReadRepository, repaymentPlanTermRepository, lenderRepayPlanPort);
    }

    @Bean
    @ConditionalOnBean(LenderRepayVaPort.class)
    RepayVaFacade repayVaFacade(
            LenderRepayVaPort lenderRepayVaPort,
            RepayVaSnapshotRepository repayVaSnapshotRepository,
            ObjectMapper objectMapper
    ) {
        return new RepayVaFacade(lenderRepayVaPort, repayVaSnapshotRepository, objectMapper);
    }

    @Bean
    @ConditionalOnBean(LenderRepayTrialPort.class)
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
    @ConditionalOnBean(LenderRepayCurrentOrderPort.class)
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
