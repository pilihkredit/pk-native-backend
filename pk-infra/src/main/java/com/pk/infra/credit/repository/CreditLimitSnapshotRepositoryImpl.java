package com.pk.infra.credit.repository;

import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.infra.credit.mapper.CreditLimitSnapshotMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CreditLimitSnapshotRepositoryImpl implements CreditLimitSnapshotRepository {
    private final CreditLimitSnapshotMapper creditLimitSnapshotMapper;

    public CreditLimitSnapshotRepositoryImpl(CreditLimitSnapshotMapper creditLimitSnapshotMapper) {
        this.creditLimitSnapshotMapper = creditLimitSnapshotMapper;
    }

    @Override
    public void upsert(CreditLimitSnapshotData data) {
        creditLimitSnapshotMapper.deleteByCreditApplicationId(data.creditApplicationId());
        creditLimitSnapshotMapper.insert(data);
    }

    @Override
    public Optional<CreditLimitSnapshotData> findByCreditApplicationId(long creditApplicationId) {
        return Optional.ofNullable(creditLimitSnapshotMapper.findByCreditApplicationId(creditApplicationId));
    }
}
