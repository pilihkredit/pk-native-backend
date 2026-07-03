package com.pk.core.loan.port;

import com.pk.core.loan.LenderLoanProduct;
import java.time.Instant;
import java.util.List;

public interface LenderProductLatestRepository {
    void replaceLatest(ReplaceLatestCommand command);

    record ReplaceLatestCommand(
            long profileId,
            long creditApplicationId,
            String mobileNo,
            String applyId,
            String creditApplyNo,
            String lenderUserId,
            String creditStatus,
            String productStatus,
            String lastLenderRequestJson,
            String lastLenderResponseJson,
            Instant fetchedAt,
            List<LenderLoanProduct> products
    ) {
    }
}
