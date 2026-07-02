package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotRecord;
import com.pk.infra.repay.repository.RepaymentTrialInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialOrderInsertParam;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RepaymentTrialSnapshotMapper {
    int insertSnapshot(RepaymentTrialInsertParam param);
    int insertOrder(RepaymentTrialOrderInsertParam param);
    TrialSnapshotRecord findByTrialNo(@Param("trialNo") String trialNo);
}
