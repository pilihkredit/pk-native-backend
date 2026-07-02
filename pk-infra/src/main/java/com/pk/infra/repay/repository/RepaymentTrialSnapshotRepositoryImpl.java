package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import com.pk.infra.repay.mapper.RepaymentTrialSnapshotMapper;
import java.time.Instant; import java.util.List; import java.util.Optional;
import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository public class RepaymentTrialSnapshotRepositoryImpl implements RepaymentTrialSnapshotRepository {
private final RepaymentTrialSnapshotMapper mapper; public RepaymentTrialSnapshotRepositoryImpl(RepaymentTrialSnapshotMapper mapper){this.mapper=mapper;}
@Override @Transactional public TrialSnapshotRecord insert(TrialSnapshotInsert insert,List<TrialOrderInsert> orders){
    RepaymentTrialInsertParam p=RepaymentTrialInsertParam.from(insert); mapper.insertSnapshot(p);
    for(TrialOrderInsert order:orders) mapper.insertOrder(RepaymentTrialOrderInsertParam.from(p.getId(),order));
    return new TrialSnapshotRecord(p.getId(),insert.trialNo(),insert.profileId(),insert.trialType(),insert.totalBillCount(),insert.totalShouldAmount(),insert.totalReductionAmount(),insert.defaultVaJson(),insert.rawResponseJson(),Instant.now());}
@Override public Optional<TrialSnapshotRecord> findByTrialNo(String trialNo){return Optional.ofNullable(mapper.findByTrialNo(trialNo));}}
