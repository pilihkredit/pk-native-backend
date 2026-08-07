package com.pk.infra.credit.repository;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.infra.credit.mapper.CreditApplicationMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CreditApplicationRepositoryImpl implements CreditApplicationRepository {
    private final CreditApplicationMapper creditApplicationMapper;

    public CreditApplicationRepositoryImpl(CreditApplicationMapper creditApplicationMapper) {
        this.creditApplicationMapper = creditApplicationMapper;
    }

    @Override
    public Optional<CreditApplicationRecord> findById(long id) {
        return Optional.ofNullable(creditApplicationMapper.findById(id));
    }

    @Override
    public Optional<CreditApplicationRecord> findByRequestId(String requestId) {
        return Optional.ofNullable(creditApplicationMapper.findByRequestId(requestId));
    }

    @Override
    public Optional<CreditApplicationRecord> findByApplyIdAndUserId(String applyId, long userId) {
        return Optional.ofNullable(creditApplicationMapper.findByApplyIdAndUserId(applyId, userId));
    }

    @Override
    public Optional<CreditApplicationRecord> findLatestByUserId(long userId) {
        return Optional.ofNullable(creditApplicationMapper.findLatestByUserId(userId));
    }

    @Override
    public Optional<CreditApplicationRecord> findByApplyId(String applyId) {
        return Optional.ofNullable(creditApplicationMapper.findByApplyId(applyId));
    }

    @Override
    public List<CreditApplicationRecord> findDueForStatusBackfill(int limit) {
        return creditApplicationMapper.findDueForStatusBackfill(limit);
    }

    @Override
    public long insert(CreditApplicationInsert insert) {
        CreditApplicationInsertParam param = CreditApplicationInsertParam.from(insert);
        creditApplicationMapper.insert(param);
        Long id = creditApplicationMapper.findIdByApplyId(insert.applyId());
        if (id == null) {
            throw new IllegalStateException("Failed to load inserted credit application");
        }
        return id;
    }

    @Override
    public void updateApplyNo(long id, String applyNo) {
        creditApplicationMapper.updateApplyNo(id, applyNo);
    }

    @Override
    public void updateLastLenderInteraction(long id, Long externalInteractionId) {
        creditApplicationMapper.updateLastLenderInteraction(id, externalInteractionId);
    }

    public static class CreditApplicationInsertParam {
        private String applyId;
        private String requestId;
        private String providerCode;
        private long userId;

        public static CreditApplicationInsertParam from(CreditApplicationInsert insert) {
            CreditApplicationInsertParam param = new CreditApplicationInsertParam();
            param.applyId = insert.applyId();
            param.requestId = insert.requestId();
            param.providerCode = insert.providerCode();
            param.userId = insert.userId();
            return param;
        }

        public String getApplyId() {
            return applyId;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getProviderCode() {
            return providerCode;
        }

        public long getUserId() {
            return userId;
        }
    }
}
