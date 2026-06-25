package com.pk.app.loan.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record UnevenBillRateResponse(int termNum, BigDecimal repaymentRate) {
}
