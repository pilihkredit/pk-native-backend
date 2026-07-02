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
    public void upsert(CreditLenderStatusQueryData data) {
        mapper.upsert(data);
    }

    @Override
    public Optional<CreditLenderStatusQueryData> findByApplyId(String applyId) {
        return Optional.ofNullable(mapper.findByApplyId(applyId));
    }

    @Override
    public Optional<CreditLenderStatusQueryData> findByApplyIdAndProfileId(String applyId, long profileId) {
        return Optional.ofNullable(mapper.findByApplyIdAndProfileId(applyId, profileId));
    }
}
