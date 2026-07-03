package com.pk.adapter.pendanaan;

import com.pk.core.loan.port.LenderLoanHistoryPort;
import java.util.List;

public class FakePendanaanLoanHistoryAdapter implements LenderLoanHistoryPort {
    @Override
    public LenderLoanHistoryResult queryHistory(String partnerUserId) {
        return new LenderLoanHistoryResult(null, List.of());
    }
}
