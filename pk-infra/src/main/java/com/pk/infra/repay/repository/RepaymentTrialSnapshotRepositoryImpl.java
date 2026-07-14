package com.pk.infra.repay.repository;

import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.LenderRepayTrialDiscountInfo;
import com.pk.core.repay.LenderRepayTrialTerm;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialOrderInsert;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotInsert;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotRecord;
import com.pk.infra.repay.RepaymentTrialPersistenceMapper;
import com.pk.infra.repay.mapper.RepaymentTrialSnapshotMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RepaymentTrialSnapshotRepositoryImpl implements RepaymentTrialSnapshotRepository {
    private final RepaymentTrialSnapshotMapper mapper;

    public RepaymentTrialSnapshotRepositoryImpl(RepaymentTrialSnapshotMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TrialSnapshotRecord insert(TrialSnapshotInsert insert, List<TrialOrderInsert> orders) {
        RepaymentTrialInsertParam snapshotParam = new RepaymentTrialInsertParam();
        RepaymentTrialPersistenceMapper.fillSnapshotInsertParam(snapshotParam, insert);
        mapper.insertSnapshot(snapshotParam);
        insertVaChannels("SNAPSHOT", snapshotParam.getId(), insert.defaultVa(), insert.spareVa(), insert.disabledDefaultVa());

        for (TrialOrderInsert order : orders) {
            RepaymentTrialOrderInsertParam orderParam = new RepaymentTrialOrderInsertParam();
            RepaymentTrialPersistenceMapper.fillOrderInsertParam(snapshotParam.getId(), orderParam, order);
            mapper.insertOrder(orderParam);
            insertVaChannels(
                    "ORDER",
                    orderParam.getId(),
                    order.bill().defaultVa(),
                    order.bill().spareVa(),
                    order.bill().disabledDefaultVa()
            );
            insertTerms(orderParam.getId(), order.bill().termInfo());
        }

        return RepaymentTrialPersistenceMapper.toRecord(snapshotParam, Instant.now());
    }

    @Override
    public Optional<TrialSnapshotRecord> findByTrialNo(String trialNo) {
        return Optional.ofNullable(mapper.findByTrialNo(trialNo));
    }

    private void insertVaChannels(String ownerType, long ownerId, LenderRepayVa defaultVa, LenderRepayVa spareVa, LenderRepayVa disabledDefaultVa) {
        for (RepaymentTrialVaChannelInsertParam channelParam
                : RepaymentTrialPersistenceMapper.toVaChannelParams(ownerType, ownerId, defaultVa, spareVa, disabledDefaultVa)) {
            mapper.insertVaChannel(channelParam);
        }
    }

    private void insertTerms(long trialOrderId, List<LenderRepayTrialTerm> terms) {
        if (terms == null || terms.isEmpty()) {
            return;
        }
        for (LenderRepayTrialTerm term : terms) {
            RepaymentTrialTermInsertParam termParam = new RepaymentTrialTermInsertParam();
            RepaymentTrialPersistenceMapper.fillTermInsertParam(trialOrderId, termParam, term);
            mapper.insertTerm(termParam);
            insertTermDiscounts(termParam.getId(), term.discountInfos());
        }
    }

    private void insertTermDiscounts(long trialTermId, List<LenderRepayTrialDiscountInfo> discounts) {
        if (discounts == null || discounts.isEmpty()) {
            return;
        }
        for (LenderRepayTrialDiscountInfo discount : discounts) {
            RepaymentTrialTermDiscountInsertParam discountParam = new RepaymentTrialTermDiscountInsertParam();
            RepaymentTrialPersistenceMapper.fillTermDiscountInsertParam(trialTermId, discountParam, discount);
            mapper.insertTermDiscount(discountParam);
        }
    }
}
