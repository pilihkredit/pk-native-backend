package com.pk.infra.credit;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.display.DisplayFormatters;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.RepaymentUniformity;
import com.pk.infra.loan.LoanProductFacade;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreditLimitDisplayFacade {
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    private final LoanProductFacade loanProductFacade;

    public CreditLimitDisplayFacade(
            CreditApplicationRepository creditApplicationRepository,
            CreditLenderStatusQueryRepository creditLenderStatusQueryRepository,
            LoanProductFacade loanProductFacade
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditLenderStatusQueryRepository = creditLenderStatusQueryRepository;
        this.loanProductFacade = loanProductFacade;
    }

    public LimitDisplayResult getLimitDisplay(long profileId, String applyId, String repayMethod) {
        if (applyId == null || applyId.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        CreditApplicationRepository.CreditApplicationRecord record = creditApplicationRepository
                .findByApplyIdAndProfileId(applyId, profileId)
                .orElseThrow(() -> new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND));
        if (!CreditApplicationStatus.APPROVED.equals(record.status())) {
            throw new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
        }

        CreditLenderStatusQueryRepository.CreditLenderStatusQueryData limits = creditLenderStatusQueryRepository
                .findByApplyIdAndProfileId(applyId, profileId)
                .orElseThrow(() -> new ApiException(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE));

        Boolean repaymentUniform = null;
        BigDecimal defaultAmount = null;
        if (repayMethod != null && !repayMethod.isBlank()) {
            LenderRepayMethod selected = loanProductFacade.findRepayMethod(profileId, applyId, repayMethod);
            if (selected == null) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            repaymentUniform = RepaymentUniformity.isUniform(selected);
            defaultAmount = Boolean.TRUE.equals(repaymentUniform)
                    ? limits.fakeCreditLimit()
                    : limits.riskMaxLimit();
        }

        boolean psychologicalEqualsFake = equals(limits.psychologicalCreditLimit(), limits.fakeCreditLimit());
        return new LimitDisplayResult(
                applyId,
                limits.riskMinLimit(),
                limits.riskMaxLimit(),
                limits.fakeCreditLimit(),
                limits.psychologicalCreditLimit(),
                limits.borrowAmtStepSize(),
                defaultAmount,
                psychologicalEqualsFake,
                repaymentUniform,
                buildSliderSegments(limits)
        );
    }

    private static List<SliderSegment> buildSliderSegments(CreditLenderStatusQueryRepository.CreditLenderStatusQueryData limits) {
        Map<String, SliderSegment> segments = new LinkedHashMap<>();
        addSegment(segments, "MIN", limits.riskMinLimit());
        addSegment(segments, "PSYCHOLOGICAL", limits.psychologicalCreditLimit());
        addSegment(segments, "FAKE", limits.fakeCreditLimit());
        addSegment(segments, "MAX_CREDIT", limits.riskMaxLimit());
        return new ArrayList<>(segments.values());
    }

    private static void addSegment(Map<String, SliderSegment> segments, String segmentType, BigDecimal value) {
        if (value == null) {
            return;
        }
        String key = value.stripTrailingZeros().toPlainString() + ":" + segmentType;
        segments.putIfAbsent(key, new SliderSegment(value, DisplayFormatters.formatIdrAmount(value), segmentType));
    }

    private static boolean equals(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    public record LimitDisplayResult(
            String applyId,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal borrowAmtStepSize,
            BigDecimal defaultAmount,
            boolean psychologicalEqualsFake,
            Boolean repaymentUniform,
            List<SliderSegment> sliderSegments
    ) {
    }

    public record SliderSegment(BigDecimal value, String valueDisplay, String segmentType) {
    }
}
