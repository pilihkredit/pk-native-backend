package com.pk.infra.repay.mapper;

import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotRecord;
import com.pk.infra.repay.repository.RepaymentTrialInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialOrderInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermDiscountInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialVaChannelInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RepaymentTrialSnapshotMapper {
    int insertSnapshot(RepaymentTrialInsertParam param);

    int insertOrder(RepaymentTrialOrderInsertParam param);

    int insertTerm(RepaymentTrialTermInsertParam param);

    int insertTermDiscount(RepaymentTrialTermDiscountInsertParam param);

    int insertVaChannel(RepaymentTrialVaChannelInsertParam param);

    TrialSnapshotRecord findByTrialNo(@Param("trialNo") String trialNo);
}
