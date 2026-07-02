package com.pk.infra.credit.repository;

import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.infra.credit.mapper.CreditStatusHistoryMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CreditStatusHistoryRepositoryImpl implements CreditStatusHistoryRepository {
    private final CreditStatusHistoryMapper creditStatusHistoryMapper;

    public CreditStatusHistoryRepositoryImpl(CreditStatusHistoryMapper creditStatusHistoryMapper) {
        this.creditStatusHistoryMapper = creditStatusHistoryMapper;
    }

    @Override
    public void insert(
            long creditApplicationId,
            String mobileNo,
            String fromStatus,
            String toStatus,
            String externalStatus,
            String source
    ) {
        creditStatusHistoryMapper.insert(
                creditApplicationId,
                mobileNo,
                fromStatus,
                toStatus,
                externalStatus,
                source
        );
    }
}
