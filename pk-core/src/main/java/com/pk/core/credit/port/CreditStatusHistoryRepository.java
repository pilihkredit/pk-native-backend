package com.pk.core.credit.port;

public interface CreditStatusHistoryRepository {
    void insert(
            long creditApplicationId,
            String mobileNo,
            String fromStatus,
            String toStatus,
            String externalStatus,
            String source
    );
}
