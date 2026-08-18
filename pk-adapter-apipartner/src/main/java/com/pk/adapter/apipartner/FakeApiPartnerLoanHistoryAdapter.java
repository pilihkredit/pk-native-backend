package com.pk.adapter.apipartner;

import com.pk.core.loan.port.LenderLoanHistoryPort;
import java.util.List;

public class FakeApiPartnerLoanHistoryAdapter implements LenderLoanHistoryPort {
    @Override
    public LenderLoanHistoryResult queryHistory(String partnerUserId) {
        return new LenderLoanHistoryResult(null, List.of());
    }
}
