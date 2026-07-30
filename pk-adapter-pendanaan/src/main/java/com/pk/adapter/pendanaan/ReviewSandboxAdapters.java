package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LenderLoanProductPort;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LenderLoanContractPort;
import com.pk.core.loan.port.LenderLoanHistoryPort;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.profile.port.LenderBankCardPort;
import com.pk.core.profile.port.LenderProfileQueryPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.repay.LenderRepayPlanTerm;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.LenderLoanBillListPort;
import com.pk.core.repay.port.LenderRepayCurrentOrderPort;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LenderRepayTrialPort;
import com.pk.core.repay.port.LenderRepayVaPort;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.core.review.ReviewSandboxConfigPort.ReviewSandboxScenario;
import com.pk.core.review.ReviewSandboxConfigPort.ReviewSandboxVa;
import com.pk.core.review.ReviewSandboxLoanDataPort;
import com.pk.core.review.ReviewSandboxLoanDataPort.ReviewLoanSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.pk.core.tracking.port.LenderTrackingPort;

final class ReviewSandboxAdapters {
    static final String PRODUCT_CODE = "REVIEW_PRODUCT";
    static final String REPAY_METHOD = "REVIEW_METHOD";
    private static final long CREDIT_CONTRACT_EXPIRE_TIME = 4_102_444_799_000L;

    private final ReviewSandboxConfigPort configPort;
    private final ReviewSandboxLoanDataPort loanDataPort;
    private final ObjectMapper objectMapper;

    ReviewSandboxAdapters(ReviewSandboxConfigPort configPort, ReviewSandboxLoanDataPort loanDataPort) {
        this(configPort, loanDataPort, new ObjectMapper());
    }

    ReviewSandboxAdapters(
            ReviewSandboxConfigPort configPort,
            ReviewSandboxLoanDataPort loanDataPort,
            ObjectMapper objectMapper
    ) {
        this.configPort = configPort;
        this.loanDataPort = loanDataPort;
        this.objectMapper = objectMapper;
    }

    LenderCreditPort credit() {
        return new LenderCreditPort() {
            @Override
            public LenderCreditApplyResult apply(LenderCreditApplyCommand command) {
                return new LenderCreditApplyResult(
                        creditNo(command.applyId()),
                        userNo(command.partnerUserId()),
                        null
                );
            }

            @Override
            public LenderCreditStatusResult queryStatus(String applyId) {
                ReviewSandboxScenario scenario = scenario();
                JsonNode template = responseTemplate(scenario, "creditStatus");
                if (template != null) {
                    return new LenderCreditStatusResult(
                            text(template, "status"),
                            text(template, "userId"),
                            text(template, "creditApplyNo"),
                            longValue(template, "creditContractExpireTime"),
                            longValue(template, "freezeEndTime"),
                            decimal(template, "riskMinLimit"),
                            decimal(template, "riskMaxLimit"),
                            decimal(template, "psychologicalCreditLimit"),
                            decimal(template, "fakeCreditLimit"),
                            decimal(template, "borrowAmtStepSize"),
                            null
                    );
                }
                return new LenderCreditStatusResult(
                        "SUCCESS",
                        "REVIEW-USER",
                        creditNo(applyId),
                        CREDIT_CONTRACT_EXPIRE_TIME,
                        null,
                        scenario.minAmount(),
                        scenario.maxAmount(),
                        scenario.maxAmount(),
                        scenario.maxAmount(),
                        scenario.amountStep(),
                        null
                );
            }
        };
    }

    LenderProfileSyncPort profileSync() {
        return new FakePendanaanProfileSyncAdapter();
    }

    LenderProfileQueryPort profileQuery() {
        return command -> new LenderProfileQueryPort.LenderProfileQueryResult("""
                {
                  "partnerUserId": "%s",
                  "userId": "%s",
                  "profile": {"status": "COMPLETE"},
                  "contact": {"status": "COMPLETE"},
                  "identity": {"status": "VERIFIED"},
                  "bankCardList": [{"bankCode": "REVIEW_BANK", "status": "VERIFIED"}],
                  "device": {"status": "ACTIVE"}
                }
                """.formatted(command.partnerUserId(), userNo(command.partnerUserId())));
    }

    LenderBankCardPort bankCard() {
        return new FakePendanaanBankCardAdapter();
    }

    LenderUserStatusPort userStatus() {
        return command -> {
            String mobileNo = LenderInteractionContext.mobileNo();
            boolean creditApproved = loanDataPort.hasApprovedCredit(mobileNo);
            boolean loanDisbursed = loanDataPort.hasDisbursedLoan(mobileNo);
            return new LenderUserStatusPort.LenderUserStatusResult(
                    command.partnerUserId(),
                    userNo(command.partnerUserId()),
                    loanDisbursed ? 4 : 2,
                    loanDisbursed ? 22 : 11,
                    null,
                    !loanDisbursed,
                    !creditApproved,
                    !loanDisbursed,
                    loanDisbursed ? 1 : 0,
                    creditApproved ? CREDIT_CONTRACT_EXPIRE_TIME : null,
                    false,
                    null
            );
        };
    }

    LenderLoanProductPort products() {
        return applyId -> {
            ReviewSandboxScenario scenario = scenario();
            JsonNode template = responseTemplate(scenario, "products");
            if (template != null) {
                return new LenderLoanProductPort.LenderLoanProductListResult(
                        applyId,
                        text(template, "creditApplyNo"),
                        text(template, "userId"),
                        text(template, "creditStatus"),
                        text(template, "productStatus"),
                        parseProducts(template.path("products")),
                        null
                );
            }
            LenderRepayMethod method = new LenderRepayMethod(
                    REPAY_METHOD,
                    "D",
                    scenario.termDays(),
                    scenario.termCount(),
                    scenario.termDays() * scenario.termCount(),
                    0,
                    null,
                    List.of()
            );
            LenderLoanProduct product = new LenderLoanProduct(
                    PRODUCT_CODE,
                    "Review Loan",
                    scenario.minAmount(),
                    scenario.maxAmount(),
                    "M",
                    scenario.comprehensiveRate(),
                    List.of(method)
            );
            return new LenderLoanProductPort.LenderLoanProductListResult(
                    applyId,
                    creditNo(applyId),
                    "REVIEW-USER",
                    "SUCCESS",
                    "READY",
                    List.of(product),
                    null
            );
        };
    }

    LenderLoanStatusPort loanStatus() {
        return loanApplyId -> {
            ReviewLoanSnapshot loan = requireLoan(loanApplyId);
            return new LenderLoanStatusPort.LenderLoanStatusResult(
                    "SUCCESS",
                    loan.loanApplyNo(),
                    loan.billNo(),
                    loan.applyAmount(),
                    payAmount(loan),
                    loan.payTime().toEpochMilli(),
                    null,
                    null
            );
        };
    }

    LenderLoanTrialPort loanTrial() {
        return command -> {
            ReviewSandboxScenario scenario = scenario();
            JsonNode template = responseTemplate(scenario, "loanTrial");
            if (template != null) {
                return parseLoanTrial(template, command);
            }
            return new FakePendanaanLoanTrialAdapter().trial(
                    command,
                    scenario.termCount(),
                    scenario.termDays(),
                    scenario.comprehensiveRate(),
                    scenario.disbursementRate()
            );
        };
    }

    LenderLoanApplyPort loanApply() {
        return command -> new LenderLoanApplyPort.LenderLoanApplyResult(
                command.loanApplyId(),
                "REVIEW-LOAN-" + command.loanApplyId(),
                "REVIEW-USER",
                "PROCESSING",
                null
        );
    }

    LenderLoanHistoryPort loanHistory() {
        return partnerUserId -> new LenderLoanHistoryPort.LenderLoanHistoryResult(
                null,
                loanDataPort.findDisbursedByPartnerUserId(partnerUserId).stream()
                        .map(loan -> new LenderLoanHistoryPort.LenderLoanHistoryOrder(
                                loan.loanApplyId(),
                                loan.loanApplyNo(),
                                loan.lenderUserId(),
                                "SUCCESS",
                                loan.billNo(),
                                loan.applyAmount(),
                                payAmount(loan),
                                loan.payTime().toEpochMilli(),
                                null,
                                loan.payTime().toEpochMilli()
                        ))
                        .toList()
        );
    }

    LenderLoanContractPort loanContracts() {
        return new FakePendanaanLoanContractAdapter();
    }

    LenderLoanBillListPort bills() {
        return (partnerUserId, billStatuses) -> new LenderLoanBillListPort.LenderLoanBillListResult(
                null,
                !billStatuses.contains("NORMAL") ? List.of() : loanDataPort.findDisbursedByPartnerUserId(partnerUserId).stream()
                        .map(this::toBill)
                        .toList()
        );
    }

    LenderRepayPlanPort repayPlan() {
        return loanApplyId -> {
            ReviewLoanSnapshot loan = requireLoan(loanApplyId);
            return new LenderRepayPlanPort.LenderRepayPlanResult(
                    loan.loanApplyId(),
                    loan.loanApplyNo(),
                    loan.billNo(),
                    buildPlan(loan),
                    null
            );
        };
    }

    LenderRepayVaPort repayVa() {
        return new LenderRepayVaPort() {
            @Override
            public LenderRepayVaListResult listVas(String partnerUserId) {
                JsonNode template = responseTemplate(scenario(), "vas");
                if (template != null) {
                    List<LenderRepayVa> configuredVas = new ArrayList<>();
                    template.path("vas").forEach(node -> configuredVas.add(PendanaanRepayVaAdapter.mapVa(node)));
                    JsonNode defaultNode = template.path("defaultVa");
                    LenderRepayVa defaultVa = defaultNode.isMissingNode() || defaultNode.isNull()
                            ? configuredVas.stream().filter(LenderRepayVa::defaultFlag).findFirst().orElse(null)
                            : PendanaanRepayVaAdapter.mapVa(defaultNode);
                    return new LenderRepayVaListResult(
                            partnerUserId, userNo(partnerUserId), defaultVa, List.copyOf(configuredVas), null
                    );
                }
                List<LenderRepayVa> vas = reviewVas();
                LenderRepayVa defaultVa = vas.stream().filter(LenderRepayVa::defaultFlag).findFirst().orElseThrow();
                return new LenderRepayVaListResult(partnerUserId, userNo(partnerUserId), defaultVa, vas, null);
            }

            @Override
            public void setDefaultVa(LenderRepayVaDefaultCommand command) {
                boolean exists = reviewVas().stream().anyMatch(va -> va.vaNo().equals(command.vaNo()));
                if (!exists) {
                    throw new IllegalArgumentException("Review sandbox VA not found");
                }
            }
        };
    }

    LenderRepayTrialPort repayTrial() {
        return new FakePendanaanRepayTrialAdapter();
    }

    LenderRepayCurrentOrderPort repayCurrentOrder() {
        return command -> {
        };
    }

    LenderTrackingPort tracking() {
        return new FakePendanaanTrackingAdapter();
    }

    private List<LenderRepayVa> reviewVas() {
        ReviewSandboxScenario scenario = scenario();
        if (!scenario.vas().isEmpty()) {
            return scenario.vas().stream().map(ReviewSandboxAdapters::toVa).toList();
        }
        return List.of(new LenderRepayVa(
                scenario.vaNo(),
                scenario.vaBankCode(),
                scenario.vaBankName(),
                0,
                null,
                List.of(),
                true,
                false,
                true
        ));
    }

    private static LenderRepayVa toVa(ReviewSandboxVa va) {
        return new LenderRepayVa(
                va.vaNo(), va.bankCode(), va.bankName(), va.bankType(), va.icon(),
                va.bankChannels().stream()
                        .map(channel -> new LenderRepayVa.LenderRepayVaChannel(
                                channel.bankChannel(), channel.instruction(), channel.defaultChannel()
                        ))
                        .toList(),
                va.defaultFlag(), va.disabled(), va.show()
        );
    }

    private List<LenderRepayPlanTerm> buildPlan(ReviewLoanSnapshot loan) {
        ReviewSandboxScenario scenario = scenario();
        BigDecimal total = loan.applyAmount()
                .multiply(BigDecimal.ONE.add(scenario.comprehensiveRate()))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal principalPerTerm = loan.applyAmount()
                .divide(BigDecimal.valueOf(scenario.termCount()), 0, RoundingMode.DOWN);
        BigDecimal amountPerTerm = total.divide(BigDecimal.valueOf(scenario.termCount()), 0, RoundingMode.DOWN);
        List<LenderRepayPlanTerm> terms = new ArrayList<>();
        BigDecimal assignedPrincipal = BigDecimal.ZERO;
        BigDecimal assignedAmount = BigDecimal.ZERO;
        for (int termNo = 1; termNo <= scenario.termCount(); termNo++) {
            boolean lastTerm = termNo == scenario.termCount();
            BigDecimal principal = lastTerm
                    ? loan.applyAmount().subtract(assignedPrincipal)
                    : principalPerTerm;
            BigDecimal amount = lastTerm ? total.subtract(assignedAmount) : amountPerTerm;
            BigDecimal interest = amount.subtract(principal);
            Instant dueDate = loan.payTime().plus((long) termNo * scenario.termDays(), ChronoUnit.DAYS);
            terms.add(new LenderRepayPlanTerm(
                    termNo,
                    dueDate,
                    loan.payTime(),
                    dueDate.plus(7, ChronoUnit.DAYS),
                    loan.billNo() + "-" + termNo,
                    "NORMAL",
                    "N",
                    amount,
                    amount,
                    principal,
                    interest,
                    BigDecimal.ZERO,
                    0,
                    null,
                    "N",
                    null
            ));
            assignedPrincipal = assignedPrincipal.add(principal);
            assignedAmount = assignedAmount.add(amount);
        }
        return List.copyOf(terms);
    }

    private LenderLoanBillListPort.LenderLoanBill toBill(ReviewLoanSnapshot loan) {
        ReviewSandboxScenario scenario = scenario();
        List<LenderRepayPlanTerm> plan = buildPlan(loan);
        LenderRepayPlanTerm first = plan.getFirst();
        BigDecimal totalInterest = loan.applyAmount().multiply(scenario.comprehensiveRate())
                .setScale(0, RoundingMode.HALF_UP);
        return new LenderLoanBillListPort.LenderLoanBill(
                loan.loanApplyId(),
                loan.loanApplyNo(),
                loan.lenderUserId(),
                loan.billNo(),
                "CONTRACT-" + loan.loanApplyId(),
                scenario.termCount(),
                loan.applyAmount(),
                "IDR",
                null,
                loan.payTime().toEpochMilli(),
                "NORMAL",
                BigDecimal.ZERO,
                payAmount(loan),
                loan.payTime().toEpochMilli(),
                null,
                plan.getLast().dueDate().toEpochMilli(),
                BigDecimal.ZERO,
                loan.applyAmount(),
                totalInterest,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                first.termNo(),
                first.dueDate().toEpochMilli(),
                BigDecimal.ZERO,
                first.shouldPrincipal(),
                first.shouldInterest(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                null,
                null,
                0,
                null,
                "N"
        );
    }

    private ReviewSandboxScenario scenario() {
        return configPort.findEnabledScenario(LenderInteractionContext.mobileNo())
                .orElseThrow(() -> new IllegalStateException("Review sandbox scenario is unavailable"));
    }

    private JsonNode responseTemplate(ReviewSandboxScenario scenario, String responseKey) {
        String raw = scenario.response(responseKey);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            return root.has("data") ? root.get("data") : root;
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid review sandbox response template: " + responseKey, exception);
        }
    }

    private List<LenderLoanProduct> parseProducts(JsonNode productNodes) {
        List<LenderLoanProduct> products = new ArrayList<>();
        for (JsonNode product : productNodes) {
            List<LenderRepayMethod> methods = new ArrayList<>();
            for (JsonNode method : product.path("repayMethods")) {
                String rawRates = text(method, "unevenBillsRepaymentRate");
                List<LenderRepayMethod.UnevenBillRate> rates = new ArrayList<>();
                if (rawRates != null && !rawRates.isBlank()) {
                    try {
                        for (JsonNode rate : objectMapper.readTree(rawRates)) {
                            rates.add(new LenderRepayMethod.UnevenBillRate(
                                    rate.path("termNum").asInt(), rate.path("repaymentRate").decimalValue()
                            ));
                        }
                    } catch (Exception exception) {
                        throw new IllegalStateException("Invalid review sandbox uneven repayment rates", exception);
                    }
                }
                methods.add(new LenderRepayMethod(
                        text(method, "repayMethod"), text(method, "cycleType"), integer(method, "cycleInterval"),
                        integer(method, "cycleCount"), integer(method, "totalCycleInterval"),
                        integer(method, "repayMethodType"), rawRates, List.copyOf(rates)
                ));
            }
            products.add(new LenderLoanProduct(
                    text(product, "productCode"), text(product, "productName"), decimal(product, "minAmount"),
                    decimal(product, "maxAmount"), text(product, "comprehensiveRateUnit"),
                    decimal(product, "comprehensiveRate"), List.copyOf(methods)
            ));
        }
        return List.copyOf(products);
    }

    private LenderLoanTrialPort.LenderLoanTrialResult parseLoanTrial(
            JsonNode template,
            LenderLoanTrialPort.LenderLoanTrialCommand command
    ) {
        try {
            ObjectNode quoteNode = ((ObjectNode) template).deepCopy();
            quoteNode.remove("termInfo");
            quoteNode.put("applyId", command.applyId());
            if (quoteNode.has("userId") && !quoteNode.has("lenderUserId")) {
                quoteNode.set("lenderUserId", quoteNode.get("userId"));
                quoteNode.remove("userId");
            }
            LoanTrialQuoteDetail quote = objectMapper.treeToValue(quoteNode, LoanTrialQuoteDetail.class);
            List<LenderTrialTerm> terms = new ArrayList<>();
            for (JsonNode termNode : template.path("termInfo")) {
                terms.add(objectMapper.treeToValue(termNode, LenderTrialTerm.class));
            }
            return new LenderLoanTrialPort.LenderLoanTrialResult(quote, List.copyOf(terms), null);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid review sandbox loan trial response template", exception);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.longValue();
    }

    private static Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.intValue();
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.decimalValue();
    }

    private ReviewLoanSnapshot requireLoan(String loanApplyId) {
        return loanDataPort.findByLoanApplyId(loanApplyId)
                .orElseThrow(() -> new IllegalStateException("Review sandbox loan not found: " + loanApplyId));
    }

    private BigDecimal payAmount(ReviewLoanSnapshot loan) {
        if (loan.payAmount() != null) {
            return loan.payAmount();
        }
        return loan.applyAmount().multiply(scenario().disbursementRate()).setScale(0, RoundingMode.HALF_UP);
    }

    private static String creditNo(String applyId) {
        return "REVIEW-CREDIT-" + applyId;
    }

    private static String userNo(String partnerUserId) {
        return "REVIEW-USER-" + partnerUserId;
    }
}
