package com.pk.adapter.apipartner;

import com.pk.core.repay.port.LenderLoanBillListPort;
import java.util.List;

public class FakeApiPartnerLoanBillListAdapter implements LenderLoanBillListPort {
    @Override
    public LenderLoanBillListResult listBills(String partnerUserId, List<String> billStatuses) {
        return new LenderLoanBillListResult(null, List.of());
    }
}
