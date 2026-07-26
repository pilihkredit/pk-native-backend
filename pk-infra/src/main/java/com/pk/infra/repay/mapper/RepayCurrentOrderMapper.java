package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.RepayCurrentOrderRepository.CurrentOrderRecord;
import com.pk.infra.repay.repository.RepayCurrentOrderInsertParam;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RepayCurrentOrderMapper {
    int deactivateActive(@Param("userId") long userId);
    int insertActive(RepayCurrentOrderInsertParam param);
    CurrentOrderRecord findActiveByUserId(@Param("userId") long userId);
}
