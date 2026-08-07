package com.pk.infra.repay;

import com.pk.core.repay.LenderRepayTrialDiscountInfo;
import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayTrialTerm;
import com.pk.core.repay.LenderRepayVa;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialOrderInsert;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotInsert;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotRecord;
import com.pk.infra.repay.repository.RepaymentTrialInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialOrderInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermDiscountInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialTermInsertParam;
import com.pk.infra.repay.repository.RepaymentTrialVaChannelInsertParam;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class RepaymentTrialPersistenceMapper {
    private RepaymentTrialPersistenceMapper() {
    }

    public static void fillSnapshotInsertParam(RepaymentTrialInsertParam param, TrialSnapshotInsert insert) {
        param.setTrialNo(insert.trialNo());
        param.setUserId(insert.userId());
        param.setTrialType(insert.trialType());
        param.setTotalBillCount(insert.totalBillCount());
        param.setTotalShouldAmount(insert.totalShouldAmount());
        param.setTotalReductionAmount(insert.totalReductionAmount());
        param.setTotalPaidAmount(insert.totalPaidAmount());
        fillSnapshotVaFields(param, insert.defaultVa(), insert.spareVa(), insert.disabledDefaultVa());
        param.setExternalInteractionId(insert.externalInteractionId());
        param.setSource(insert.source());
    }

    public static void fillOrderInsertParam(long trialId, RepaymentTrialOrderInsertParam param, TrialOrderInsert order) {
        LenderRepayTrialResult bill = order.bill();
        param.setTrialId(trialId);
        param.setLoanApplicationId(order.loanApplicationId());
        param.setSettle(order.settle());
        param.setTermNosJson(order.termNosJson());
        param.setLoanApplyId(bill.loanApplyId());
        param.setLoanApplyNo(bill.loanApplyNo());
        param.setBillNo(bill.billNo());
        param.setShouldAmount(bill.shouldAmount());
        param.setBillStatus(bill.billStatus());
        param.setName(bill.name());
        param.setUserId(bill.userId());
        param.setAdvSetteFlag(bill.advSetteFlag());
        param.setCurrency(bill.currency());
        param.setApplyAmt(bill.applyAmt());
        param.setApplyTime(bill.applyTime());
        param.setAuditTime(bill.auditTime());
        param.setLoanTime(bill.loanTime());
        param.setLoanDays(bill.loanDays());
        param.setRepayMethodType(bill.repayMethodType());
        param.setSchdAmount(bill.schdAmount());
        param.setSchdStampDuty(bill.schdStampDuty());
        param.setSchdPrincipal(bill.schdPrincipal());
        param.setSchdInterest(bill.schdInterest());
        param.setSchdFee1(bill.schdFee1());
        param.setSchdFee2(bill.schdFee2());
        param.setSchdFee3(bill.schdFee3());
        param.setSchdFee1Tax(bill.schdFee1Tax());
        param.setSchdFee2Tax(bill.schdFee2Tax());
        param.setSchdFee3Tax(bill.schdFee3Tax());
        param.setPrePenInterest(bill.prePenInterest());
        param.setPenInterest(bill.penInterest());
        param.setInitLateFee(bill.initLateFee());
        param.setShouldStampDuty(bill.shouldStampDuty());
        param.setShouldPrincipal(bill.shouldPrincipal());
        param.setShouldInterest(bill.shouldInterest());
        param.setFee1Name(bill.fee1Name());
        param.setShouldFee1(bill.shouldFee1());
        param.setFee2Name(bill.fee2Name());
        param.setShouldFee2(bill.shouldFee2());
        param.setFee3Name(bill.fee3Name());
        param.setShouldFee3(bill.shouldFee3());
        param.setShouldFee1Tax(bill.shouldFee1Tax());
        param.setShouldFee2Tax(bill.shouldFee2Tax());
        param.setShouldFee3Tax(bill.shouldFee3Tax());
        param.setShouldFee(bill.shouldFee());
        param.setShouldPenInterest(bill.shouldPenInterest());
        param.setShouldInitLateFee(bill.shouldInitLateFee());
        param.setShouldAdvSettleFee(bill.shouldAdvSettleFee());
        param.setShouldPenalty(bill.shouldPenalty());
        param.setReductionAmount(bill.reductionAmount());
        param.setReductionStampDuty(bill.reductionStampDuty());
        param.setReductionPrincipal(bill.reductionPrincipal());
        param.setReductionInterest(bill.reductionInterest());
        param.setReductionFee1(bill.reductionFee1());
        param.setReductionFee2(bill.reductionFee2());
        param.setReductionFee3(bill.reductionFee3());
        param.setReductionFee1Tax(bill.reductionFee1Tax());
        param.setReductionFee2Tax(bill.reductionFee2Tax());
        param.setReductionFee3Tax(bill.reductionFee3Tax());
        param.setReductionPenInterest(bill.reductionPenInterest());
        param.setReductionInitLateFee(bill.reductionInitLateFee());
        param.setCouponAmount(bill.couponAmount());
        param.setPartRepayFlag(bill.partRepayFlag());
        param.setPaidAmount(bill.paidAmount());
        param.setCouponId(bill.couponId());
        param.setCouponType(bill.couponType());
        param.setCouponName(bill.couponName());
        param.setUserValideDisTime(bill.userValideDisTime());
        param.setTermNo(bill.termNo());
        param.setTermDueDate(bill.termDueDate());
        fillOrderVaFields(param, bill.defaultVa(), bill.spareVa(), bill.disabledDefaultVa());
    }

    public static void fillTermInsertParam(long trialOrderId, RepaymentTrialTermInsertParam param, LenderRepayTrialTerm term) {
        param.setTrialOrderId(trialOrderId);
        param.setBillNo(term.billNo());
        param.setLoanApplyNo(term.loanApplyNo());
        param.setTermNo(term.termNo());
        param.setAdvSetteFlag(term.advSetteFlag());
        param.setSchdAmount(term.schdAmount());
        param.setSchdStampDuty(term.schdStampDuty());
        param.setSchdPrincipal(term.schdPrincipal());
        param.setSchdInterest(term.schdInterest());
        param.setSchdAllFee(term.schdAllFee());
        param.setSchdAllTaxFee(term.schdAllTaxFee());
        param.setSchdFee1(term.schdFee1());
        param.setSchdFee2(term.schdFee2());
        param.setSchdFee3(term.schdFee3());
        param.setSchdFee1Tax(term.schdFee1Tax());
        param.setSchdFee2Tax(term.schdFee2Tax());
        param.setSchdFee3Tax(term.schdFee3Tax());
        param.setPrePenInterest(term.prePenInterest());
        param.setPenInterest(term.penInterest());
        param.setInitLateFee(term.initLateFee());
        param.setDueDate(term.dueDate());
        param.setOverdueDays(term.overdueDays());
        param.setGraceDate(term.graceDate());
        param.setTermStatus(term.termStatus());
        param.setDaysOfDueDate(term.daysOfDueDate());
        param.setShouldAmount(term.shouldAmount());
        param.setShouldStampDuty(term.shouldStampDuty());
        param.setShouldPrincipal(term.shouldPrincipal());
        param.setShouldInterest(term.shouldInterest());
        param.setShouldFee1(term.shouldFee1());
        param.setShouldFee2(term.shouldFee2());
        param.setShouldFee3(term.shouldFee3());
        param.setShouldFee1Tax(term.shouldFee1Tax());
        param.setShouldFee2Tax(term.shouldFee2Tax());
        param.setShouldFee3Tax(term.shouldFee3Tax());
        param.setShouldPenInterest(term.shouldPenInterest());
        param.setShouldInitLateFee(term.shouldInitLateFee());
        param.setPartRepayFlag(term.partRepayFlag());
        param.setPaidAmount(term.paidAmount());
        param.setCouponDiscount(term.couponDiscount());
        param.setReductionAmount(term.reductionAmount());
    }

    public static void fillTermDiscountInsertParam(
            long trialTermId,
            RepaymentTrialTermDiscountInsertParam param,
            LenderRepayTrialDiscountInfo discount
    ) {
        param.setTrialTermId(trialTermId);
        param.setReductionAllAmt(discount.reductionAllAmt());
        param.setReductionType(discount.reductionType());
        param.setReductionStampDuty(discount.reductionStampDuty());
        param.setReductionPrincipal(discount.reductionPrincipal());
        param.setReductionInterest(discount.reductionInterest());
        param.setReductionFee1(discount.reductionFee1());
        param.setReductionFee2(discount.reductionFee2());
        param.setReductionFee3(discount.reductionFee3());
        param.setReductionFee1Tax(discount.reductionFee1Tax());
        param.setReductionFee2Tax(discount.reductionFee2Tax());
        param.setReductionFee3Tax(discount.reductionFee3Tax());
        param.setReductionPenInterest(discount.reductionPenInterest());
        param.setReductionInitLateFee(discount.reductionInitLateFee());
        param.setCouponId(discount.couponId());
        param.setCouponType(discount.couponType());
    }

    public static List<RepaymentTrialVaChannelInsertParam> toVaChannelParams(
            String ownerType,
            long ownerId,
            LenderRepayVa defaultVa,
            LenderRepayVa spareVa,
            LenderRepayVa disabledDefaultVa
    ) {
        List<RepaymentTrialVaChannelInsertParam> params = new ArrayList<>();
        addVaChannels(params, ownerType, ownerId, "DEFAULT", defaultVa);
        addVaChannels(params, ownerType, ownerId, "SPARE", spareVa);
        addVaChannels(params, ownerType, ownerId, "DISABLED_DEFAULT", disabledDefaultVa);
        return params;
    }

    public static TrialSnapshotRecord toRecord(RepaymentTrialInsertParam param, Instant createdAt) {
        return new TrialSnapshotRecord(
                param.getId(),
                param.getTrialNo(),
                param.getUserId(),
                param.getTrialType(),
                param.getTotalBillCount(),
                param.getTotalShouldAmount(),
                param.getTotalReductionAmount(),
                param.getTotalPaidAmount(),
                param.getExternalInteractionId(),
                createdAt
        );
    }

    private static void fillSnapshotVaFields(
            RepaymentTrialInsertParam param,
            LenderRepayVa defaultVa,
            LenderRepayVa spareVa,
            LenderRepayVa disabledDefaultVa
    ) {
        fillVaFields(
                defaultVa,
                param::setDefaultVaNo,
                param::setDefaultVaBankCode,
                param::setDefaultVaBankName,
                param::setDefaultVaBankType,
                param::setDefaultVaIcon,
                param::setDefaultVaDefaultFlag,
                param::setDefaultVaDisabled,
                param::setDefaultVaShow
        );
        fillVaFields(
                spareVa,
                param::setSpareVaNo,
                param::setSpareVaBankCode,
                param::setSpareVaBankName,
                param::setSpareVaBankType,
                param::setSpareVaIcon,
                param::setSpareVaDefaultFlag,
                param::setSpareVaDisabled,
                param::setSpareVaShow
        );
        fillVaFields(
                disabledDefaultVa,
                param::setDisabledDefaultVaNo,
                param::setDisabledDefaultVaBankCode,
                param::setDisabledDefaultVaBankName,
                param::setDisabledDefaultVaBankType,
                param::setDisabledDefaultVaIcon,
                param::setDisabledDefaultVaDefaultFlag,
                param::setDisabledDefaultVaDisabled,
                param::setDisabledDefaultVaShow
        );
    }

    private static void fillOrderVaFields(
            RepaymentTrialOrderInsertParam param,
            LenderRepayVa defaultVa,
            LenderRepayVa spareVa,
            LenderRepayVa disabledDefaultVa
    ) {
        fillVaFields(
                defaultVa,
                param::setDefaultVaNo,
                param::setDefaultVaBankCode,
                param::setDefaultVaBankName,
                param::setDefaultVaBankType,
                param::setDefaultVaIcon,
                param::setDefaultVaDefaultFlag,
                param::setDefaultVaDisabled,
                param::setDefaultVaShow
        );
        fillVaFields(
                spareVa,
                param::setSpareVaNo,
                param::setSpareVaBankCode,
                param::setSpareVaBankName,
                param::setSpareVaBankType,
                param::setSpareVaIcon,
                param::setSpareVaDefaultFlag,
                param::setSpareVaDisabled,
                param::setSpareVaShow
        );
        fillVaFields(
                disabledDefaultVa,
                param::setDisabledDefaultVaNo,
                param::setDisabledDefaultVaBankCode,
                param::setDisabledDefaultVaBankName,
                param::setDisabledDefaultVaBankType,
                param::setDisabledDefaultVaIcon,
                param::setDisabledDefaultVaDefaultFlag,
                param::setDisabledDefaultVaDisabled,
                param::setDisabledDefaultVaShow
        );
    }

    private static void fillVaFields(
            LenderRepayVa va,
            java.util.function.Consumer<String> vaNoSetter,
            java.util.function.Consumer<String> bankCodeSetter,
            java.util.function.Consumer<String> bankNameSetter,
            java.util.function.Consumer<Integer> bankTypeSetter,
            java.util.function.Consumer<String> iconSetter,
            java.util.function.Consumer<Boolean> defaultFlagSetter,
            java.util.function.Consumer<Boolean> disabledSetter,
            java.util.function.Consumer<Boolean> showSetter
    ) {
        if (va == null) {
            return;
        }
        vaNoSetter.accept(va.vaNo());
        bankCodeSetter.accept(va.bankCode());
        bankNameSetter.accept(va.bankName());
        bankTypeSetter.accept(va.bankType());
        iconSetter.accept(va.icon());
        defaultFlagSetter.accept(va.defaultFlag());
        disabledSetter.accept(va.disabled());
        showSetter.accept(va.show());
    }

    private static void addVaChannels(
            List<RepaymentTrialVaChannelInsertParam> target,
            String ownerType,
            long ownerId,
            String vaRole,
            LenderRepayVa va
    ) {
        if (va == null || va.bankChannels() == null) {
            return;
        }
        for (LenderRepayVa.LenderRepayVaChannel channel : va.bankChannels()) {
            RepaymentTrialVaChannelInsertParam param = new RepaymentTrialVaChannelInsertParam();
            param.setOwnerType(ownerType);
            param.setOwnerId(ownerId);
            param.setVaRole(vaRole);
            param.setBankChannel(channel.bankChannel());
            param.setInstruction(channel.instruction());
            param.setDefaultChannel(channel.defaultChannel());
            target.add(param);
        }
    }
}
