package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.RepayCurrentOrderRepository.CurrentOrderRecord;
import com.pk.infra.repay.repository.RepayCurrentOrderInsertParam;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RepayCurrentOrderMapper {
    int deactivateActive(@Param("profileId") long profileId);
    int insertActive(RepayCurrentOrderInsertParam param);
    CurrentOrderRecord findActiveByProfileId(@Param("profileId") long profileId);
}
