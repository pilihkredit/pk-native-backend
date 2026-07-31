package com.pk.infra.credit.repository;

import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.infra.credit.mapper.CreditLenderStatusQueryMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CreditLenderStatusQueryRepositoryImpl implements CreditLenderStatusQueryRepository {
    private final CreditLenderStatusQueryMapper mapper;

    public CreditLenderStatusQueryRepositoryImpl(CreditLenderStatusQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(CreditLenderStatusQueryData data) {
        mapper.insert(data);
    }

    @Override
    public Optional<CreditLenderStatusQueryData> findLatestByApplyId(String applyId) {
        return Optional.ofNullable(mapper.findLatestByApplyId(applyId));
    }

    @Override
    public Optional<CreditLenderStatusQueryData> findLatestByApplyIdAndUserId(String applyId, long userId) {
        return Optional.ofNullable(mapper.findLatestByApplyIdAndUserId(applyId, userId));
    }
}
