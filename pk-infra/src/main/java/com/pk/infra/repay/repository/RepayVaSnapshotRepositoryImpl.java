package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepayVaSnapshotRepository;
import com.pk.infra.repay.mapper.RepayVaSnapshotMapper;
import java.time.Instant; import java.util.List;
import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository public class RepayVaSnapshotRepositoryImpl implements RepayVaSnapshotRepository {
private final RepayVaSnapshotMapper mapper; public RepayVaSnapshotRepositoryImpl(RepayVaSnapshotMapper mapper){this.mapper=mapper;}
@Override @Transactional public void replaceSnapshots(long profileId,String snapshotNo,List<VaSnapshotInsert> snapshots,Instant fetchedAt){
    for(VaSnapshotInsert s:snapshots) mapper.insertSnapshot(new RepayVaSnapshotInsertParam(profileId,snapshotNo,s.vaNo(),s.bankCode(),s.bankName(),s.defaultFlag(),s.disabled(),s.bankChannelsJson(),fetchedAt));}
@Override public List<VaSnapshotRecord> findLatestByProfileId(long profileId){return mapper.findLatestByProfileId(profileId);}}
