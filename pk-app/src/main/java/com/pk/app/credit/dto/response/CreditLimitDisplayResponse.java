package com.pk.app.credit.dto.response;

import com.pk.core.display.DisplayFormatters;
import com.pk.infra.credit.CreditLimitDisplayFacade;
import java.math.BigDecimal;
import java.util.List;

public record CreditLimitDisplayResponse(
        String applyId,
        BigDecimal riskMinLimit,
        String riskMinLimitDisplay,
        BigDecimal riskMaxLimit,
        String riskMaxLimitDisplay,
        BigDecimal fakeCreditLimit,
        String fakeCreditLimitDisplay,
        BigDecimal psychologicalCreditLimit,
        String psychologicalCreditLimitDisplay,
        BigDecimal borrowAmtStepSize,
        String borrowAmtStepSizeDisplay,
        BigDecimal defaultAmount,
        String defaultAmountDisplay,
        boolean psychologicalEqualsFake,
        Boolean repaymentUniform,
        List<SliderSegmentResponse> sliderSegments
) {
    public static CreditLimitDisplayResponse from(CreditLimitDisplayFacade.LimitDisplayResult result) {
        return new CreditLimitDisplayResponse(
                result.applyId(),
                result.riskMinLimit(),
                DisplayFormatters.formatIdrAmount(result.riskMinLimit()),
                result.riskMaxLimit(),
                DisplayFormatters.formatIdrAmount(result.riskMaxLimit()),
                result.fakeCreditLimit(),
                DisplayFormatters.formatIdrAmount(result.fakeCreditLimit()),
                result.psychologicalCreditLimit(),
                DisplayFormatters.formatIdrAmount(result.psychologicalCreditLimit()),
                result.borrowAmtStepSize(),
                DisplayFormatters.formatIdrAmount(result.borrowAmtStepSize()),
                result.defaultAmount(),
                DisplayFormatters.formatIdrAmount(result.defaultAmount()),
                result.psychologicalEqualsFake(),
                result.repaymentUniform(),
                result.sliderSegments().stream()
                        .map(segment -> new SliderSegmentResponse(
                                segment.value(),
                                segment.valueDisplay(),
                                segment.segmentType()
                        ))
                        .toList()
        );
    }
}
