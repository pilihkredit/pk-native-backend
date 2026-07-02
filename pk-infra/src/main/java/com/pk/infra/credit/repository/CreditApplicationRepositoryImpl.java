package com.pk.infra.credit.repository;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.infra.credit.mapper.CreditApplicationMapper;
import java.time.Instant;
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
    public Optional<CreditApplicationRecord> findByApplyIdAndProfileId(String applyId, long profileId) {
        return Optional.ofNullable(creditApplicationMapper.findByApplyIdAndProfileId(applyId, profileId));
    }

    @Override
    public Optional<CreditApplicationRecord> findByApplyId(String applyId) {
        return Optional.ofNullable(creditApplicationMapper.findByApplyId(applyId));
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
    public void updateStatus(long id, String status, String externalStatus, String lastErrorCode) {
        creditApplicationMapper.updateStatus(id, status, externalStatus, lastErrorCode);
    }

    @Override
    public void markSubmitted(long id, String externalCreditApplyNo, String externalStatus) {
        creditApplicationMapper.markSubmitted(id, externalCreditApplyNo, externalStatus);
    }

    @Override
    public void scheduleNextPoll(long id, Instant nextPollAt) {
        creditApplicationMapper.scheduleNextPoll(id, nextPollAt);
    }

    @Override
    public void updateFreezeEndAt(long id, Instant freezeEndAt) {
        creditApplicationMapper.updateFreezeEndAt(id, freezeEndAt);
    }

    @Override
    public List<CreditApplicationRecord> findDueForPoll(int limit) {
        return creditApplicationMapper.findDueForPoll(limit);
    }

    public static class CreditApplicationInsertParam {
        private String applyId;
        private String requestId;
        private String providerCode;
        private long profileId;
        private String mobileNo;
        private long profileVersionId;
        private String status;

        public static CreditApplicationInsertParam from(CreditApplicationInsert insert) {
            CreditApplicationInsertParam param = new CreditApplicationInsertParam();
            param.applyId = insert.applyId();
            param.requestId = insert.requestId();
            param.providerCode = insert.providerCode();
            param.profileId = insert.profileId();
            param.mobileNo = insert.mobileNo();
            param.profileVersionId = insert.profileVersionId();
            param.status = insert.status();
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

        public long getProfileId() {
            return profileId;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public long getProfileVersionId() {
            return profileVersionId;
        }

        public String getStatus() {
            return status;
        }
    }
}
