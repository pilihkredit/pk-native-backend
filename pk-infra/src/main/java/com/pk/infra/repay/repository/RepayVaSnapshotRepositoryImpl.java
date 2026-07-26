package com.pk.infra.repay.repository;

import com.pk.core.repay.port.RepayVaSnapshotRepository;
import com.pk.infra.repay.mapper.RepayVaSnapshotMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RepayVaSnapshotRepositoryImpl implements RepayVaSnapshotRepository {
    private final RepayVaSnapshotMapper mapper;
    public RepayVaSnapshotRepositoryImpl(RepayVaSnapshotMapper mapper){this.mapper=mapper;}
    @Override @Transactional public void replaceSnapshots(long userId,String snapshotNo,List<VaSnapshotInsert> snapshots,Long externalInteractionId,Instant fetchedAt){
        for(VaSnapshotInsert s:snapshots) mapper.insertSnapshot(new RepayVaSnapshotInsertParam(userId,snapshotNo,s.vaNo(),s.bankCode(),s.bankName(),s.defaultFlag(),s.disabled(),s.bankChannelsJson(),externalInteractionId,fetchedAt));}
    @Override public List<VaSnapshotRecord> findLatestByUserId(long userId){return mapper.findLatestByUserId(userId);}
}
