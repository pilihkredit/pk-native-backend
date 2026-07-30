package com.pk.core.review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewSandboxConfigPort {
    Optional<ReviewSandboxScenario> findEnabledScenario(String mobileNo);

    record ReviewSandboxScenario(
            String code,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal amountStep,
            BigDecimal comprehensiveRate,
            BigDecimal disbursementRate,
            int termCount,
            int termDays,
            String vaBankCode,
            String vaBankName,
            String vaNo,
            List<ReviewSandboxVa> vas,
            Map<String, String> responses
    ) {
        public ReviewSandboxScenario(
                String code,
                BigDecimal minAmount,
                BigDecimal maxAmount,
                BigDecimal amountStep,
                BigDecimal comprehensiveRate,
                BigDecimal disbursementRate,
                int termCount,
                int termDays,
                String vaBankCode,
                String vaBankName,
                String vaNo
        ) {
            this(code, minAmount, maxAmount, amountStep, comprehensiveRate, disbursementRate,
                    termCount, termDays, vaBankCode, vaBankName, vaNo, List.of(), Map.of());
        }

        public ReviewSandboxScenario(
                String code,
                BigDecimal minAmount,
                BigDecimal maxAmount,
                BigDecimal amountStep,
                BigDecimal comprehensiveRate,
                BigDecimal disbursementRate,
                int termCount,
                int termDays,
                String vaBankCode,
                String vaBankName,
                String vaNo,
                List<ReviewSandboxVa> vas
        ) {
            this(code, minAmount, maxAmount, amountStep, comprehensiveRate, disbursementRate,
                    termCount, termDays, vaBankCode, vaBankName, vaNo, vas, Map.of());
        }

        public String response(String responseKey) {
            return responses.get(responseKey);
        }
    }

    record ReviewSandboxVa(
            String vaNo,
            String bankCode,
            String bankName,
            Integer bankType,
            String icon,
            List<ReviewSandboxVaChannel> bankChannels,
            boolean defaultFlag,
            boolean disabled,
            boolean show
    ) {
    }

    record ReviewSandboxVaChannel(
            String bankChannel,
            String instruction,
            boolean defaultChannel
    ) {
    }
}
