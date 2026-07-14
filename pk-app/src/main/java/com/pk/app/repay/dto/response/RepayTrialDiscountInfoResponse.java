package com.pk.app.repay.dto.response;

import com.pk.core.display.DisplayFormatters;
import com.pk.core.repay.LenderRepayTrialDiscountInfo;
import java.math.BigDecimal;

public record RepayTrialDiscountInfoResponse(
        BigDecimal reductionAllAmt,
        String reductionAllAmtDisplay,
        String reductionType,
        BigDecimal reductionStampDuty,
        String reductionStampDutyDisplay,
        BigDecimal reductionPrincipal,
        String reductionPrincipalDisplay,
        BigDecimal reductionInterest,
        String reductionInterestDisplay,
        BigDecimal reductionFee1,
        String reductionFee1Display,
        BigDecimal reductionFee2,
        String reductionFee2Display,
        BigDecimal reductionFee3,
        String reductionFee3Display,
        BigDecimal reductionFee1Tax,
        String reductionFee1TaxDisplay,
        BigDecimal reductionFee2Tax,
        String reductionFee2TaxDisplay,
        BigDecimal reductionFee3Tax,
        String reductionFee3TaxDisplay,
        BigDecimal reductionPenInterest,
        String reductionPenInterestDisplay,
        BigDecimal reductionInitLateFee,
        String reductionInitLateFeeDisplay,
        Long couponId,
        String couponType
) {
    public static RepayTrialDiscountInfoResponse from(LenderRepayTrialDiscountInfo info) {
        return new RepayTrialDiscountInfoResponse(
                info.reductionAllAmt(),
                DisplayFormatters.formatIdrAmount(info.reductionAllAmt()),
                info.reductionType(),
                info.reductionStampDuty(),
                DisplayFormatters.formatIdrAmount(info.reductionStampDuty()),
                info.reductionPrincipal(),
                DisplayFormatters.formatIdrAmount(info.reductionPrincipal()),
                info.reductionInterest(),
                DisplayFormatters.formatIdrAmount(info.reductionInterest()),
                info.reductionFee1(),
                DisplayFormatters.formatIdrAmount(info.reductionFee1()),
                info.reductionFee2(),
                DisplayFormatters.formatIdrAmount(info.reductionFee2()),
                info.reductionFee3(),
                DisplayFormatters.formatIdrAmount(info.reductionFee3()),
                info.reductionFee1Tax(),
                DisplayFormatters.formatIdrAmount(info.reductionFee1Tax()),
                info.reductionFee2Tax(),
                DisplayFormatters.formatIdrAmount(info.reductionFee2Tax()),
                info.reductionFee3Tax(),
                DisplayFormatters.formatIdrAmount(info.reductionFee3Tax()),
                info.reductionPenInterest(),
                DisplayFormatters.formatIdrAmount(info.reductionPenInterest()),
                info.reductionInitLateFee(),
                DisplayFormatters.formatIdrAmount(info.reductionInitLateFee()),
                info.couponId(),
                info.couponType()
        );
    }
}
