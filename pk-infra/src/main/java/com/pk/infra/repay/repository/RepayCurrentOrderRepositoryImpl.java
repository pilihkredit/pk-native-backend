package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepayCurrentOrderRepository;
import com.pk.infra.repay.mapper.RepayCurrentOrderMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository public class RepayCurrentOrderRepositoryImpl implements RepayCurrentOrderRepository {
private final RepayCurrentOrderMapper mapper; public RepayCurrentOrderRepositoryImpl(RepayCurrentOrderMapper mapper){this.mapper=mapper;}
@Override @Transactional public CurrentOrderRecord upsertActive(CurrentOrderUpsert command){
    mapper.deactivateActive(command.userId()); RepayCurrentOrderInsertParam p=RepayCurrentOrderInsertParam.from(command);
    mapper.insertActive(p); return new CurrentOrderRecord(p.getId(),command.userId(),command.currentOrderNo(),command.trialId(),command.repayOrdersJson(),command.couponId(),"ACTIVE",command.submittedAt());}
@Override public Optional<CurrentOrderRecord> findActiveByUserId(long userId){return Optional.ofNullable(mapper.findActiveByUserId(userId));}}
