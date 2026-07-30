package com.pk.adapter.pendanaan;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.external.LenderInteractionContext;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.core.review.ReviewSandboxLoanDataPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewSandboxLenderAdapterTest {
    private ReviewSandboxAdapters adapters;

    @BeforeEach
    void setUp() {
        ReviewSandboxConfigPort config = mobileNo -> Optional.of(scenario());
        ReviewSandboxLoanDataPort loanData = new ReviewSandboxLoanDataPort() {
            @Override
            public Optional<ReviewLoanSnapshot> findByLoanApplyId(String loanApplyId) {
                return Optional.of(loan());
            }

            @Override
            public List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(String partnerUserId) {
                return List.of(loan());
            }
        };
        adapters = new ReviewSandboxAdapters(config, loanData);
        LenderInteractionContext.setMobileNo("628111000000");
    }

    @AfterEach
    void clearContext() {
        LenderInteractionContext.clear();
    }

    @Test
    void returnsApprovedCreditAndAvailableProduct() {
        var credit = adapters.credit().queryStatus("APPLY-1");
        var repeatedCredit = adapters.credit().queryStatus("APPLY-1");
        var products = adapters.products().listProducts("APPLY-1");

        assertThat(credit.externalStatus()).isEqualTo("SUCCESS");
        assertThat(credit.fakeCreditLimit()).isEqualByComparingTo("3000000");
        assertThat(credit.creditContractExpireTime()).isEqualTo(4102444799000L);
        assertThat(repeatedCredit.creditContractExpireTime()).isEqualTo(credit.creditContractExpireTime());
        assertThat(products.products()).hasSize(1);
        assertThat(products.products().getFirst().maxAmount()).isEqualByComparingTo("3000000");
    }

    @Test
    void returnsAllRequiredOnboardingModules() throws Exception {
        String raw = adapters.profileQuery().query(
                new com.pk.core.profile.port.LenderProfileQueryPort.LenderProfileQueryCommand(
                        "USER-1", List.of("profile", "contact", "identity", "bankCard", "device")
                )
        ).rawResponseJson();
        var root = new ObjectMapper().readTree(raw);

        assertThat(root.path("profile").isEmpty()).isFalse();
        assertThat(root.path("contact").isEmpty()).isFalse();
        assertThat(root.path("identity").isEmpty()).isFalse();
        assertThat(root.path("bankCardList").isEmpty()).isFalse();
        assertThat(root.path("device").isEmpty()).isFalse();
    }

    @Test
    void returnsDisbursedLoanWithBillsPlanAndVa() {
        var status = adapters.loanStatus().queryStatus("LOAN-1");
        var bills = adapters.bills().listBills("USER-1", List.of("NORMAL"));
        var plan = adapters.repayPlan().fetchPlan("LOAN-1");
        var vas = adapters.repayVa().listVas("USER-1");

        assertThat(status.externalStatus()).isEqualTo("SUCCESS");
        assertThat(status.billNo()).isEqualTo("BILL-LOAN-1");
        assertThat(bills.bills()).hasSize(1);
        assertThat(bills.bills().getFirst().billStatus()).isEqualTo("NORMAL");
        assertThat(plan.terms()).hasSize(6);
        assertThat(plan.terms()).allMatch(term -> "NORMAL".equals(term.termStatus()));
        assertThat(vas.defaultVa().vaNo()).isEqualTo("0000000000000000");
        assertThat(adapters.bills().listBills("USER-1", List.of("SETTLE")).bills()).isEmpty();
        assertThat(adapters.loanHistory().queryHistory("USER-1").orders()).hasSize(1);

        BigDecimal totalTerms = plan.terms().stream()
                .map(com.pk.core.repay.LenderRepayPlanTerm::shouldAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalTerms).isEqualByComparingTo("1180000");
    }

    @Test
    void calculatesLoanTrialFromReviewScenario() {
        var result = adapters.loanTrial().trial(
                new com.pk.core.loan.port.LenderLoanTrialPort.LenderLoanTrialCommand(
                        "APPLY-1", new BigDecimal("1000000"),
                        ReviewSandboxAdapters.PRODUCT_CODE, ReviewSandboxAdapters.REPAY_METHOD, null
                )
        );

        assertThat(result.quote().loanTerm()).isEqualTo(6);
        assertThat(result.quote().payAmount()).isEqualByComparingTo("970000");
        assertThat(result.quote().schdAmount()).isEqualByComparingTo("1180000");
        assertThat(result.termInfo()).hasSize(6);
    }

    @Test
    void updatesUserStatusFromPersistedReviewProgress() {
        ReviewSandboxLoanDataPort progress = new ReviewSandboxLoanDataPort() {
            @Override
            public Optional<ReviewLoanSnapshot> findByLoanApplyId(String loanApplyId) {
                return Optional.empty();
            }

            @Override
            public List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(String partnerUserId) {
                return List.of();
            }

            @Override
            public boolean hasApprovedCredit(String mobileNo) {
                return true;
            }

            @Override
            public boolean hasDisbursedLoan(String mobileNo) {
                return true;
            }
        };
        ReviewSandboxAdapters progressAdapters = new ReviewSandboxAdapters(
                mobileNo -> Optional.of(scenario()), progress, new ObjectMapper()
        );

        var status = progressAdapters.userStatus().queryStatus(
                new com.pk.core.home.port.LenderUserStatusPort.LenderUserStatusCommand("USER-1", null)
        );

        assertThat(status.firstCreditApply()).isFalse();
        assertThat(status.firstLoanApply()).isFalse();
        assertThat(status.firstLoan()).isFalse();
        assertThat(status.onLoanCount()).isEqualTo(1);
    }

    @Test
    void returnsConfiguredResponseTemplatesWithoutRecalculatingValues() {
        var configured = scenarioWithResponses(Map.of(
                "creditStatus", """
                        {"status":"SUCCESS","creditApplyNo":"CREDIT-TEMPLATE","userId":"USER-TEMPLATE",
                         "creditContractExpireTime":4102444799000,"riskMinLimit":600000,"riskMaxLimit":6000000,
                         "psychologicalCreditLimit":7000000,"fakeCreditLimit":12000000,"borrowAmtStepSize":200000}
                        """,
                "vas", """
                        {"defaultVa":{"vaNo":"1111111111111","bankCode":"BANK_A","bankName":"Bank A",
                         "bankType":1,"icon":null,"bankChannels":[],"defaultFlag":true,"disabled":false,"show":true},
                         "vas":[{"vaNo":"1111111111111","bankCode":"BANK_A","bankName":"Bank A",
                         "bankType":1,"icon":null,"bankChannels":[],"defaultFlag":true,"disabled":false,"show":true}]}
                        """,
                "products", """
                        {"creditApplyNo":"CREDIT-TEMPLATE","userId":"USER-TEMPLATE","creditStatus":"SUCCESS",
                         "productStatus":"READY","products":[{"productCode":"PRODUCT-TEMPLATE","productName":"Review Product",
                         "minAmount":200000,"maxAmount":6000000,"comprehensiveRateUnit":"D","comprehensiveRate":0.003,
                         "repayMethods":[{"repayMethod":"METHOD-TEMPLATE","cycleType":"D","cycleInterval":5,
                         "cycleCount":3,"totalCycleInterval":110,"repayMethodType":1,
                         "unevenBillsRepaymentRate":"[{\\\"repaymentRate\\\":0.70,\\\"termNum\\\":3}]"}]}]}
                        """,
                "loanTrial", """
                        {"creditApplyNo":"CREDIT-TEMPLATE","userId":"USER-TEMPLATE","applyAmt":600000,
                         "productCode":"PRODUCT-TEMPLATE","repayMethod":"METHOD-TEMPLATE","loanTerm":3,
                         "loanPrincipal":660000,"showLoanPrincipal":660000,"payAmount":600000,"handFee":60000,
                         "schdAmount":847001,"shouldAmount":847001,"interest":163855,"dayRate":0.003,
                         "showDayRate":0.00244,"totalDays":110,"unevenBillsFlag":true,
                         "termInfo":[{"termNo":1,"schdAmount":592903,"shouldAmount":592903,
                         "shouldPrincipal":462000,"shouldInterest":114700}]}
                        """
        ));
        ReviewSandboxConfigPort config = mobileNo -> Optional.of(configured);
        ReviewSandboxAdapters templateAdapters = new ReviewSandboxAdapters(
                config,
                loanData(),
                new ObjectMapper()
        );

        var credit = templateAdapters.credit().queryStatus("APPLY-2");
        var vas = templateAdapters.repayVa().listVas("USER-2");
        var products = templateAdapters.products().listProducts("APPLY-2");
        var trial = templateAdapters.loanTrial().trial(
                new com.pk.core.loan.port.LenderLoanTrialPort.LenderLoanTrialCommand(
                        "APPLY-2", new BigDecimal("600000"), "PRODUCT-TEMPLATE", "METHOD-TEMPLATE", null
                )
        );

        assertThat(credit.fakeCreditLimit()).isEqualByComparingTo("12000000");
        assertThat(credit.riskMinLimit()).isEqualByComparingTo("600000");
        assertThat(vas.vas()).hasSize(1);
        assertThat(vas.defaultVa().vaNo()).isEqualTo("1111111111111");
        assertThat(products.products().getFirst().productCode()).isEqualTo("PRODUCT-TEMPLATE");
        assertThat(products.products().getFirst().repayMethods().getFirst().totalCycleInterval()).isEqualTo(110);
        assertThat(trial.quote().schdAmount()).isEqualByComparingTo("847001");
        assertThat(trial.termInfo()).hasSize(1);
    }

    private static ReviewSandboxConfigPort.ReviewSandboxScenario scenario() {
        return new ReviewSandboxConfigPort.ReviewSandboxScenario(
                "APP_STORE", new BigDecimal("500000"), new BigDecimal("3000000"),
                new BigDecimal("100000"), new BigDecimal("0.18"), new BigDecimal("0.97"), 6, 30,
                "REVIEW_BANK", "Review Bank", "0000000000000000"
        );
    }

    private static ReviewSandboxConfigPort.ReviewSandboxScenario scenarioWithResponses(Map<String, String> responses) {
        var base = scenario();
        return new ReviewSandboxConfigPort.ReviewSandboxScenario(
                base.code(), base.minAmount(), base.maxAmount(), base.amountStep(), base.comprehensiveRate(),
                base.disbursementRate(), base.termCount(), base.termDays(), base.vaBankCode(), base.vaBankName(),
                base.vaNo(), base.vas(), responses
        );
    }

    private static ReviewSandboxLoanDataPort loanData() {
        return new ReviewSandboxLoanDataPort() {
            @Override
            public Optional<ReviewLoanSnapshot> findByLoanApplyId(String loanApplyId) {
                return Optional.of(loan());
            }

            @Override
            public List<ReviewLoanSnapshot> findDisbursedByPartnerUserId(String partnerUserId) {
                return List.of(loan());
            }
        };
    }

    private static ReviewSandboxLoanDataPort.ReviewLoanSnapshot loan() {
        return new ReviewSandboxLoanDataPort.ReviewLoanSnapshot(
                "LOAN-1", "REVIEW-LOAN-LOAN-1", "USER-1", "REVIEW-USER-USER-1",
                "BILL-LOAN-1", new BigDecimal("1000000"), new BigDecimal("970000"),
                Instant.parse("2026-07-30T00:00:00Z")
        );
    }
}
