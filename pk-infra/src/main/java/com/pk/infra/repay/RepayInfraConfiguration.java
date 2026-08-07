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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties({
        RepayTrialProperties.class,
        RepayTrialBackfillProperties.class
})
public class RepayInfraConfiguration {
    @Bean
    LoanBillsFacade loanBillsFacade(
            LenderLoanBillListPort lenderLoanBillListPort,
            LoanLenderBillRepository loanLenderBillRepository
    ) {
        return new LoanBillsFacade(lenderLoanBillListPort, loanLenderBillRepository);
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
    RepayTrialBackfillService repayTrialBackfillService(
            LoanBillReadRepository loanBillReadRepository,
            RepaymentPlanTermRepository repaymentPlanTermRepository,
            RepayTrialFacade repayTrialFacade,
            com.pk.core.auth.port.UserAuthRepository userAuthRepository,
            RepayTrialBackfillProperties repayTrialBackfillProperties
    ) {
        return new RepayTrialBackfillService(
                loanBillReadRepository,
                repaymentPlanTermRepository,
                repayTrialFacade,
                userAuthRepository,
                repayTrialBackfillProperties
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
