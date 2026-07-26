package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class CreditLenderStatusApplier {
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;

    public CreditLenderStatusApplier(CreditLenderStatusQueryRepository creditLenderStatusQueryRepository) {
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
    }

    public void apply(
            CreditApplicationRepository.CreditApplicationRecord record,
            LenderCreditPort.LenderCreditStatusResult status,
            String source,
            String limitSource
    ) {
        apply(record, status, source, limitSource, null);
    }

    public void apply(
            CreditApplicationRepository.CreditApplicationRecord record,
            LenderCreditPort.LenderCreditStatusResult status,
            String source,
            String limitSource,
            Long externalInteractionCallbackId
    ) {
        Optional<CreditLenderStatusQueryRepository.CreditLenderStatusQueryData> latest =
                creditLenderStatusQueryRepository.findLatestByApplyId(record.applyId());

        CreditLenderStatusQueryRepository.CreditLenderStatusQueryData next =
                new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                        record.applyId(),
                        record.userId(),
                        record.mobileNo(),
                        record.partnerUserId(),
                        status.lenderUserId(),
                        status.creditApplyNo(),
                        status.externalStatus(),
                        status.creditContractExpireTime(),
                        status.freezeEndTime(),
                        status.riskMinLimit(),
                        status.riskMaxLimit(),
                        status.psychologicalCreditLimit(),
                        status.fakeCreditLimit(),
                        status.borrowAmtStepSize(),
                        status.externalInteractionId(),
                        externalInteractionCallbackId,
                        Instant.now()
                );

        if (latest.isPresent() && !hasBusinessChange(latest.get(), next)) {
            return;
        }
        creditLenderStatusQueryRepository.insert(next);
    }

    static boolean hasBusinessChange(
            CreditLenderStatusQueryRepository.CreditLenderStatusQueryData previous,
            CreditLenderStatusQueryRepository.CreditLenderStatusQueryData next
    ) {
        return !Objects.equals(normalizeText(previous.externalStatus()), normalizeText(next.externalStatus()))
                || !Objects.equals(previous.creditContractExpireTime(), next.creditContractExpireTime())
                || !Objects.equals(previous.freezeEndTime(), next.freezeEndTime())
                || !decimalEquals(previous.riskMinLimit(), next.riskMinLimit())
                || !decimalEquals(previous.riskMaxLimit(), next.riskMaxLimit())
                || !decimalEquals(previous.psychologicalCreditLimit(), next.psychologicalCreditLimit())
                || !decimalEquals(previous.fakeCreditLimit(), next.fakeCreditLimit())
                || !decimalEquals(previous.borrowAmtStepSize(), next.borrowAmtStepSize())
                || !Objects.equals(normalizeText(previous.lenderUserId()), normalizeText(next.lenderUserId()))
                || !Objects.equals(normalizeText(previous.creditApplyNo()), normalizeText(next.creditApplyNo()));
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean decimalEquals(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }
}
