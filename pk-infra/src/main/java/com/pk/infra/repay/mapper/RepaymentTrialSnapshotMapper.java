package com.pk.infra.repay.mapper;

import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotRecord;
import com.pk.infra.repay.repository.RepaymentTrialInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialOrderInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermDiscountInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialVaChannelInsertParam;
import java.util.List;
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

    List<TrialOrderRef> findOrdersByLoanApplyIds(@Param("loanApplyIds") List<String> loanApplyIds);

    int deleteTermDiscountsByOrderId(@Param("orderId") long orderId);

    int deleteTermsByOrderId(@Param("orderId") long orderId);

    int deleteVaChannelsByOwner(@Param("ownerType") String ownerType, @Param("ownerId") long ownerId);

    int deleteOrderById(@Param("orderId") long orderId);

    int countOrdersByTrialId(@Param("trialId") long trialId);

    int deleteSnapshotById(@Param("snapshotId") long snapshotId);

    record TrialOrderRef(long orderId, long trialId) {
    }
}
